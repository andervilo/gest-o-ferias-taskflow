package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.application.dto.UpdateEmployeeCommand;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.employee.infrastructure.EmployeeMapper;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEmployeeUseCase {

    private final EmployeeRepository repo;
    private final CurrentUserProvider currentUserProvider;
    private final EmployeeMapper mapper;

    @Transactional
    public EmployeeResult execute(UpdateEmployeeCommand cmd) {
        if (!currentUserProvider.getCurrentUser().isAdmin()) {
            throw new ForbiddenDomainException("Apenas administradores podem editar colaboradores.");
        }
        Employee employee = repo.findById(cmd.id())
                .orElseThrow(() -> new NotFoundException("Colaborador não encontrado: " + cmd.id()));

        String newEmail = cmd.email();
        if (!employee.getEmail().value().equals(newEmail.trim().toLowerCase())
                && repo.existsByEmail(newEmail)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "E-mail já em uso: " + newEmail);
        }
        if (employee.getRole() == Role.COLLABORATOR) {
            if (cmd.managerId() == null) {
                throw new ValidationDomainException("Colaborador deve ter um manager informado.");
            }
            Employee manager = repo.findById(cmd.managerId())
                    .orElseThrow(() -> new NotFoundException("Manager não encontrado: " + cmd.managerId()));
            if (manager.getRole() != Role.MANAGER) {
                throw new ValidationDomainException("O usuário indicado não possui o papel MANAGER.");
            }
        }
        employee.update(cmd.name(), cmd.email(), cmd.managerId());
        return mapper.toResult(repo.save(employee));
    }
}