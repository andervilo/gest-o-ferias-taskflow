package com.taskflow.gestaocolaboradores.employee.application;

import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeFilter;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.employee.infrastructure.EmployeeMapper;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListEmployeesUseCase {

    private final EmployeeRepository repo;
    private final CurrentUserProvider currentUserProvider;
    private final EmployeeMapper mapper;

    @Transactional(readOnly = true)
    public PagedResult<EmployeeResult> execute(EmployeeFilter filter, int page, int size,
                                               String sortBy, String sortDir) {
        CurrentUser current = currentUserProvider.getCurrentUser();

        EmployeeFilter scoped = switch (current.role()) {
            case ADMIN -> filter;
            case MANAGER -> new EmployeeFilter(filter.query(), filter.role(), current.id(), filter.includeInactive());
            case COLLABORATOR -> throw new ForbiddenDomainException("Colaboradores não podem listar usuários.");
        };

        PagedResult<com.taskflow.gestaocolaboradores.employee.domain.Employee> domainPage =
                repo.findAll(scoped, page, size, sortBy, sortDir);

        return new PagedResult<>(
                domainPage.content().stream().map(mapper::toResult).toList(),
                domainPage.page(),
                domainPage.size(),
                domainPage.totalElements(),
                domainPage.totalPages()
        );
    }
}