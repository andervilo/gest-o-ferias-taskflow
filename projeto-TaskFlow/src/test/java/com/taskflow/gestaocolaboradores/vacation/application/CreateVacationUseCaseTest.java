package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import com.taskflow.gestaocolaboradores.vacation.application.dto.CreateVacationCommand;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationPeriod;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequest;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateVacationUseCaseTest {

    @Mock VacationRequestRepository vacationRepo;
    @Mock EmployeeRepository employeeRepo;
    @Mock CurrentUserProvider currentUserProvider;
    @Mock VacationMapper mapper;
    @InjectMocks CreateVacationUseCase useCase;

    private static final UUID EMP_ID = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2026, 8, 1);
    private static final LocalDate END = LocalDate.of(2026, 8, 5);

    private Employee activeEmployee;

    @BeforeEach
    void setUp() {
        activeEmployee = Employee.reconstitute(EMP_ID, "Alice", "alice@test.com",
                Role.COLLABORATOR, MANAGER_ID, "hash", true, null, null);
        when(employeeRepo.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee));
        when(vacationRepo.findActivePeriodsExcludingEmployee(any(), any(), any()))
                .thenReturn(List.of());
        when(vacationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResult(any())).thenAnswer(inv -> {
            VacationRequest r = inv.getArgument(0);
            return new VacationResult(r.getId(), r.getEmployeeId(), null, null,
                    r.getPeriod().startDate(), r.getPeriod().endDate(),
                    r.getStatus(), r.getReason(), r.getDecidedBy(), r.getDecidedAt(),
                    r.getCreatedAt(), r.getUpdatedAt());
        });
    }

    @Test
    void collaborator_canCreateForSelf() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(EMP_ID, Role.COLLABORATOR));
        var cmd = new CreateVacationCommand(null, START, END, "reason");
        var result = useCase.execute(cmd);
        assertThat(result.employeeId()).isEqualTo(EMP_ID);
        assertThat(result.status()).isEqualTo(VacationStatus.PENDING);
    }

    @Test
    void collaborator_tryingToCreateForOther_throws403() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(EMP_ID, Role.COLLABORATOR));
        var other = UUID.randomUUID();
        var cmd = new CreateVacationCommand(other, START, END, null);
        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(ForbiddenDomainException.class);
    }

    @Test
    void admin_canCreateForAnyEmployee() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(UUID.randomUUID(), Role.ADMIN));
        var cmd = new CreateVacationCommand(EMP_ID, START, END, null);
        var result = useCase.execute(cmd);
        assertThat(result.employeeId()).isEqualTo(EMP_ID);
    }

    @Test
    void overlapWithActiveVacation_throws409() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(EMP_ID, Role.COLLABORATOR));
        when(vacationRepo.findActivePeriodsExcludingEmployee(any(), any(), any()))
                .thenReturn(List.of(new VacationPeriod(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7))));
        var cmd = new CreateVacationCommand(null, START, END, null);
        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("sobrepõe");
    }

    @Test
    void inactiveEmployee_throws404() {
        var inactiveEmp = Employee.reconstitute(EMP_ID, "Alice", "alice@test.com",
                Role.COLLABORATOR, MANAGER_ID, "hash", false, null, null);
        when(employeeRepo.findById(EMP_ID)).thenReturn(Optional.of(inactiveEmp));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(EMP_ID, Role.COLLABORATOR));
        var cmd = new CreateVacationCommand(null, START, END, null);
        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(com.taskflow.gestaocolaboradores.shared.domain.NotFoundException.class);
    }
}
