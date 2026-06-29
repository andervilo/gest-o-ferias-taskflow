package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.application.dto.CreateEmployeeCommand;
import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.employee.infrastructure.EmployeeMapper;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateEmployeeUseCase {

    private final EmployeeRepository repo;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper mapper;

    @Transactional
    public EmployeeResult execute(CreateEmployeeCommand cmd) {
        if (!currentUserProvider.getCurrentUser().isAdmin()) {
            throw new ForbiddenDomainException("Apenas administradores podem criar colaboradores.");
        }
        if (repo.existsByEmail(cmd.email())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "E-mail já cadastrado: " + cmd.email());
        }
        if (cmd.role() == Role.COLLABORATOR) {
            if (cmd.managerId() == null) {
                throw new ValidationDomainException("Colaborador deve ter um manager informado.");
            }
            Employee manager = repo.findById(cmd.managerId())
                    .orElseThrow(() -> new NotFoundException("Manager não encontrado: " + cmd.managerId()));
            if (manager.getRole() != Role.MANAGER) {
                throw new ValidationDomainException("O usuário indicado não possui o papel MANAGER.");
            }
        }
        String hash = passwordEncoder.encode(cmd.password());
        Employee employee = Employee.create(cmd.name(), cmd.email(), cmd.role(), cmd.managerId(), hash);
        return mapper.toResult(repo.save(employee));
    }
}
