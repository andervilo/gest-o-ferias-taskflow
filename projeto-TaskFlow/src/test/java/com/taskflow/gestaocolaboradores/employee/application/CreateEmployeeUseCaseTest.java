package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.application.dto.CreateEmployeeCommand;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.employee.infrastructure.EmployeeMapper;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateEmployeeUseCaseTest {

    @Mock EmployeeRepository repo;
    @Mock CurrentUserProvider userProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmployeeMapper mapper;

    @InjectMocks CreateEmployeeUseCase useCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();

    @BeforeEach
    void setupEncoder() {
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
    }

    @Test
    void onlyAdminCanCreate() {
        when(userProvider.getCurrentUser())
                .thenReturn(new CurrentUser(UUID.randomUUID(), Role.MANAGER));
        assertThrows(ForbiddenDomainException.class,
                () -> useCase.execute(new CreateEmployeeCommand("X", "x@x.com", "pass", Role.COLLABORATOR, MANAGER_ID)));
    }

    @Test
    void duplicateEmailThrows() {
        when(userProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, Role.ADMIN));
        when(repo.existsByEmail(anyString())).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> useCase.execute(new CreateEmployeeCommand("X", "x@x.com", "pass", Role.ADMIN, null)));
    }

    @Test
    void collaboratorWithMissingManagerThrows() {
        when(userProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, Role.ADMIN));
        when(repo.existsByEmail(anyString())).thenReturn(false);
        assertThrows(ValidationDomainException.class,
                () -> useCase.execute(new CreateEmployeeCommand("Carla", "c@x.com", "pass", Role.COLLABORATOR, null)));
    }

    @Test
    void collaboratorWithNonExistentManagerThrows() {
        when(userProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, Role.ADMIN));
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.findById(MANAGER_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> useCase.execute(new CreateEmployeeCommand("Carla", "c@x.com", "pass", Role.COLLABORATOR, MANAGER_ID)));
    }

    @Test
    void collaboratorWithNonManagerRoleThrows() {
        when(userProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, Role.ADMIN));
        when(repo.existsByEmail(anyString())).thenReturn(false);
        Employee wrongRole = Employee.create("A", "a@x.com", Role.ADMIN, null, "hash");
        when(repo.findById(MANAGER_ID)).thenReturn(Optional.of(wrongRole));
        assertThrows(ValidationDomainException.class,
                () -> useCase.execute(new CreateEmployeeCommand("Carla", "c@x.com", "pass", Role.COLLABORATOR, MANAGER_ID)));
    }

    @Test
    void validAdminCreation() {
        when(userProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, Role.ADMIN));
        when(repo.existsByEmail(anyString())).thenReturn(false);
        Employee saved = Employee.create("Ana", "ana@x.com", Role.ADMIN, null, "hash");
        when(repo.save(any())).thenReturn(saved);
        when(mapper.toResult(any())).thenReturn(null);

        useCase.execute(new CreateEmployeeCommand("Ana", "ana@x.com", "pass", Role.ADMIN, null));
    }
}