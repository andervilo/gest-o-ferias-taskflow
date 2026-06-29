package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.vacation.application.dto.CalendarEntryResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCalendarEntriesUseCase {

    private final VacationRequestRepository vacationRepo;
    private final EmployeeRepository employeeRepo;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<CalendarEntryResult> execute(LocalDate from, LocalDate to) {
        var current = currentUserProvider.getCurrentUser();

        List<UUID> scope = null;
        if (current.isCollaborator()) {
            scope = List.of(current.id());
        } else if (current.isManager()) {
            scope = employeeRepo.findByManagerId(current.id())
                    .stream().map(Employee::getId).toList();
        }

        var vacations = vacationRepo.findActiveInPeriod(from, to, scope);

        // build id→name map to avoid N+1 lookups
        var employeeIds = vacations.stream()
                .map(v -> v.getEmployeeId())
                .collect(Collectors.toSet());

        Map<UUID, String> nameById = employeeRepo.findAllById(employeeIds)
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        return vacations.stream()
                .map(v -> new CalendarEntryResult(
                        v.getId(),
                        v.getEmployeeId(),
                        nameById.getOrDefault(v.getEmployeeId(), "Colaborador"),
                        v.getPeriod().startDate(),
                        v.getPeriod().endDate(),
                        v.getStatus()))
                .toList();
    }
}
