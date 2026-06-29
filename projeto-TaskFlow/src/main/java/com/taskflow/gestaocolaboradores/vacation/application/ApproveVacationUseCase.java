package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.OverlapPolicy;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationPeriod;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveVacationUseCase {

    private final VacationRequestRepository vacationRepo;
    private final EmployeeRepository employeeRepo;
    private final CurrentUserProvider currentUserProvider;
    private final VacationMapper mapper;
    private final OverlapPolicy overlapPolicy = new OverlapPolicy();

    @Transactional
    public VacationResult execute(UUID id) {
        var current = currentUserProvider.getCurrentUser();
        var request = vacationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido de férias não encontrado: " + id));

        if (current.isManager()) {
            var employee = employeeRepo.findById(request.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException("Colaborador não encontrado."));
            if (!current.id().equals(employee.getManagerId())) {
                throw new ForbiddenDomainException("Você só pode aprovar férias dos seus colaboradores.");
            }
        } else if (!current.isAdmin()) {
            throw new ForbiddenDomainException("Apenas managers e admins podem aprovar pedidos.");
        }

        // Re-verifica sobreposição antes de aprovar (autoridade final)
        var period = request.getPeriod();
        var existing = vacationRepo.findApprovedPeriodsExcludingEmployee(
                request.getEmployeeId(), period.startDate(), period.endDate());
        if (overlapPolicy.overlapsAny(new VacationPeriod(period.startDate(), period.endDate()), existing)) {
            throw new ConflictException("VACATION_OVERLAP",
                    "O período solicitado passou a sobrepor-se às férias de outro colaborador.");
        }

        request.approve(current.id());
        return mapper.toResult(vacationRepo.save(request));
    }
}
