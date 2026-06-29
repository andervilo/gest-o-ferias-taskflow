package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import com.taskflow.gestaocolaboradores.vacation.application.dto.CreateVacationCommand;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.OverlapPolicy;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequest;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateVacationUseCase {

    private final VacationRequestRepository vacationRepo;
    private final EmployeeRepository employeeRepo;
    private final CurrentUserProvider currentUserProvider;
    private final VacationMapper mapper;
    private final OverlapPolicy overlapPolicy = new OverlapPolicy();

    @Transactional
    public VacationResult execute(CreateVacationCommand cmd) {
        var current = currentUserProvider.getCurrentUser();

        UUID targetEmployeeId;
        if (current.isAdmin()) {
            targetEmployeeId = cmd.employeeId() != null ? cmd.employeeId() : current.id();
        } else {
            targetEmployeeId = current.id();
            if (cmd.employeeId() != null && !cmd.employeeId().equals(current.id())) {
                throw new ForbiddenDomainException("Você só pode solicitar férias para si mesmo.");
            }
        }

        employeeRepo.findById(targetEmployeeId)
                .filter(e -> e.isActive())
                .orElseThrow(() -> new NotFoundException("Colaborador não encontrado: " + targetEmployeeId));

        var existing = vacationRepo.findActivePeriodsExcludingEmployee(
                targetEmployeeId, cmd.startDate(), cmd.endDate());
        if (overlapPolicy.overlapsAny(
                new com.taskflow.gestaocolaboradores.vacation.domain.VacationPeriod(cmd.startDate(), cmd.endDate()),
                existing)) {
            throw new ConflictException("VACATION_OVERLAP",
                    "O período solicitado sobrepõe-se às férias de outro colaborador.");
        }

        VacationRequest request = VacationRequest.create(targetEmployeeId, cmd.startDate(), cmd.endDate(), cmd.reason());
        return mapper.toResult(vacationRepo.save(request));
    }
}