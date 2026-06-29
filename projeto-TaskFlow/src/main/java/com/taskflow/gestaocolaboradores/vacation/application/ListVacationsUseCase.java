package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationFilter;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequest;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListVacationsUseCase {

    private final VacationRequestRepository vacationRepo;
    private final EmployeeRepository employeeRepo;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PagedResult<VacationResult> execute(UUID filterEmployeeId, UUID filterManagerId,
                                               String filterName,
                                               List<VacationStatus> filterStatuses,
                                               LocalDate filterFrom, LocalDate filterTo,
                                               int page, int size,
                                               String sortBy, String sortDir) {
        CurrentUser current = currentUserProvider.getCurrentUser();
        VacationFilter filter = buildFilter(current, filterEmployeeId, filterManagerId,
                filterName, filterStatuses, filterFrom, filterTo);

        var domainPage = vacationRepo.findAll(filter, page, size, sortBy, sortDir);

        var employeeIds = domainPage.content().stream()
                .map(VacationRequest::getEmployeeId)
                .collect(Collectors.toSet());
        Map<UUID, Employee> employeeById = employeeRepo.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        Set<UUID> managerIds = employeeById.values().stream()
                .map(Employee::getManagerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> managerNameById = managerIds.isEmpty() ? Map.of()
                : employeeRepo.findAllById(managerIds).stream()
                        .collect(Collectors.toMap(Employee::getId, Employee::getName));

        var results = domainPage.content().stream()
                .map(v -> {
                    Employee emp = employeeById.get(v.getEmployeeId());
                    String empName = emp != null ? emp.getName() : null;
                    String mgrName = emp != null && emp.getManagerId() != null
                            ? managerNameById.get(emp.getManagerId()) : null;
                    return toResult(v, empName, mgrName);
                })
                .toList();

        return new PagedResult<>(results, domainPage.page(), domainPage.size(),
                domainPage.totalElements(), domainPage.totalPages());
    }

    private VacationResult toResult(VacationRequest v, String employeeName, String managerName) {
        return new VacationResult(
                v.getId(), v.getEmployeeId(), employeeName, managerName,
                v.getPeriod().startDate(), v.getPeriod().endDate(),
                v.getStatus(), v.getReason(),
                v.getDecidedBy(), v.getDecidedAt(),
                v.getCreatedAt(), v.getUpdatedAt());
    }

    private VacationFilter buildFilter(CurrentUser current, UUID filterEmployeeId,
                                       UUID filterManagerId, String filterName,
                                       List<VacationStatus> filterStatuses,
                                       LocalDate filterFrom, LocalDate filterTo) {
        if (current.isCollaborator()) {
            return new VacationFilter(List.of(current.id()), filterStatuses, filterFrom, filterTo);
        }
        if (current.isManager()) {
            Set<UUID> subordinateIds = new HashSet<>(employeeRepo.findByManagerId(current.id())
                    .stream().map(Employee::getId).collect(Collectors.toSet()));
            if (filterName != null && !filterName.isBlank()) {
                Set<UUID> nameIds = employeeRepo.findByNameContainingIgnoreCase(filterName.trim())
                        .stream().map(Employee::getId).collect(Collectors.toSet());
                subordinateIds.retainAll(nameIds);
            }
            return new VacationFilter(List.copyOf(subordinateIds), filterStatuses, filterFrom, filterTo);
        }
        Set<UUID> ids = null;
        if (filterEmployeeId != null) {
            ids = new HashSet<>(Set.of(filterEmployeeId));
        } else if (filterManagerId != null) {
            ids = new HashSet<>(employeeRepo.findByManagerId(filterManagerId)
                    .stream().map(Employee::getId).collect(Collectors.toSet()));
        }
        if (filterName != null && !filterName.isBlank()) {
            Set<UUID> nameIds = employeeRepo.findByNameContainingIgnoreCase(filterName.trim())
                    .stream().map(Employee::getId).collect(Collectors.toSet());
            if (ids != null) {
                ids.retainAll(nameIds);
            } else {
                ids = nameIds;
            }
        }
        return new VacationFilter(ids == null ? null : List.copyOf(ids),
                filterStatuses, filterFrom, filterTo);
    }
}