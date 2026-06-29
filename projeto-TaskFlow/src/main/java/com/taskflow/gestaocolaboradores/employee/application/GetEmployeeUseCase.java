package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.employee.infrastructure.EmployeeMapper;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEmployeeUseCase {

    private final EmployeeRepository repo;
    private final CurrentUserProvider currentUserProvider;
    private final EmployeeMapper mapper;

    @Transactional(readOnly = true)
    public EmployeeResult execute(UUID id) {
        CurrentUser current = currentUserProvider.getCurrentUser();
        Employee employee = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Colaborador não encontrado: " + id));

        if (current.isAdmin()) {
            return mapper.toResult(employee);
        }
        if (current.isManager()) {
            List<Employee> subordinates = repo.findByManagerId(current.id());
            boolean isMine = subordinates.stream().anyMatch(e -> e.getId().equals(id));
            if (!isMine) throw new ForbiddenDomainException("Acesso negado a este colaborador.");
            return mapper.toResult(employee);
        }
        if (!current.id().equals(id)) {
            throw new ForbiddenDomainException("Colaboradores só podem visualizar o próprio cadastro.");
        }
        return mapper.toResult(employee);
    }
}