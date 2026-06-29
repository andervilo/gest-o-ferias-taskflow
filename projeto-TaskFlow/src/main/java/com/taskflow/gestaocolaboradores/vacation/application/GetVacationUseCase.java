package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequest;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetVacationUseCase {

    private final VacationRequestRepository vacationRepo;
    private final EmployeeRepository employeeRepo;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public VacationResult execute(UUID id) {
        var current = currentUserProvider.getCurrentUser();
        var request = vacationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido de férias não encontrado: " + id));

        if (current.isAdmin()) {
            return withName(request);
        }
        if (current.isCollaborator()) {
            if (!request.getEmployeeId().equals(current.id())) {
                throw new ForbiddenDomainException("Você não tem acesso a este pedido.");
            }
            return withName(request);
        }
        var employee = employeeRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Colaborador não encontrado."));
        if (!current.id().equals(employee.getManagerId())) {
            throw new ForbiddenDomainException("Você não tem acesso a este pedido.");
        }
        return withName(request, employee.getName());
    }

    private VacationResult withName(VacationRequest v) {
        String name = employeeRepo.findById(v.getEmployeeId())
                .map(Employee::getName).orElse(null);
        return withName(v, name);
    }

    private VacationResult withName(VacationRequest v, String employeeName) {
        return new VacationResult(
                v.getId(), v.getEmployeeId(), employeeName, null,
                v.getPeriod().startDate(), v.getPeriod().endDate(),
                v.getStatus(), v.getReason(),
                v.getDecidedBy(), v.getDecidedAt(),
                v.getCreatedAt(), v.getUpdatedAt());
    }
}