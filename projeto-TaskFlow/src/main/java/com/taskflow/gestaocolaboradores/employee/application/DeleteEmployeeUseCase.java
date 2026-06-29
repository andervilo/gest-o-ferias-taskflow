package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteEmployeeUseCase {

    private final EmployeeRepository repo;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void execute(UUID id) {
        if (!currentUserProvider.getCurrentUser().isAdmin()) {
            throw new ForbiddenDomainException("Apenas administradores podem remover colaboradores.");
        }
        Employee employee = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Colaborador não encontrado: " + id));
        employee.deactivate();
        repo.save(employee);
    }
}