package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.*;
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
class ApproveVacationUseCaseTest {

    @Mock VacationRequestRepository vacationRepo;
    @Mock EmployeeRepository employeeRepo;
    @Mock CurrentUserProvider currentUserProvider;
    @Mock VacationMapper mapper;
    @InjectMocks ApproveVacationUseCase useCase;

    private static final UUID EMP_ID = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();
    private static final UUID REQ_ID = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2026, 8, 1);
    private static final LocalDate END = LocalDate.of(2026, 8, 5);

    private VacationRequest pendingRequest;

    @BeforeEach
    void setUp() {
        pendingRequest = VacationRequest.create(EMP_ID, START, END, null);
        var employee = Employee.reconstitute(EMP_ID, "Alice", "alice@test.com",
                Role.COLLABORATOR, MANAGER_ID, "hash", true, null, null);

        when(vacationRepo.findById(REQ_ID)).thenReturn(Optional.of(pendingRequest));
        when(employeeRepo.findById(EMP_ID)).thenReturn(Optional.of(employee));
        when(vacationRepo.findApprovedPeriodsExcludingEmployee(any(), any(), any()))
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
    void admin_canApprove() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(UUID.randomUUID(), Role.ADMIN));
        var result = useCase.execute(REQ_ID);
        assertThat(result.status()).isEqualTo(VacationStatus.APPROVED);
    }

    @Test
    void manager_owningEmployee_canApprove() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(MANAGER_ID, Role.MANAGER));
        var result = useCase.execute(REQ_ID);
        assertThat(result.status()).isEqualTo(VacationStatus.APPROVED);
    }

    @Test
    void manager_notOwningEmployee_throws403() {
        var otherId = UUID.randomUUID();
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(otherId, Role.MANAGER));
        assertThatThrownBy(() -> useCase.execute(REQ_ID))
                .isInstanceOf(ForbiddenDomainException.class);
    }

    @Test
    void collaborator_cannotApprove() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(EMP_ID, Role.COLLABORATOR));
        assertThatThrownBy(() -> useCase.execute(REQ_ID))
                .isInstanceOf(ForbiddenDomainException.class);
    }

    @Test
    void overlapDetectedOnApprove_throws409() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(UUID.randomUUID(), Role.ADMIN));
        when(vacationRepo.findApprovedPeriodsExcludingEmployee(any(), any(), any()))
                .thenReturn(List.of(new VacationPeriod(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7))));
        assertThatThrownBy(() -> useCase.execute(REQ_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("sobrepor");
    }
}