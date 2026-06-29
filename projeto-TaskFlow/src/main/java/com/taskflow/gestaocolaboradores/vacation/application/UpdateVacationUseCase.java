package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import com.taskflow.gestaocolaboradores.vacation.application.dto.UpdateVacationCommand;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.OverlapPolicy;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationPeriod;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVacationUseCase {

    private final VacationRequestRepository vacationRepo;
    private final CurrentUserProvider currentUserProvider;
    private final VacationMapper mapper;
    private final OverlapPolicy overlapPolicy = new OverlapPolicy();

    @Transactional
    public VacationResult execute(UpdateVacationCommand cmd) {
        var current = currentUserProvider.getCurrentUser();
        var request = vacationRepo.findById(cmd.id())
                .orElseThrow(() -> new NotFoundException("Pedido de férias não encontrado: " + cmd.id()));

        if (!current.isAdmin() && !request.getEmployeeId().equals(current.id())) {
            throw new ForbiddenDomainException("Você não tem permissão para editar este pedido.");
        }

        var existing = vacationRepo.findActivePeriodsExcludingEmployee(
                request.getEmployeeId(), cmd.startDate(), cmd.endDate());
        if (overlapPolicy.overlapsAny(new VacationPeriod(cmd.startDate(), cmd.endDate()), existing)) {
            throw new ConflictException("VACATION_OVERLAP",
                    "O período solicitado sobrepõe-se às férias de outro colaborador.");
        }

        request.update(cmd.startDate(), cmd.endDate(), cmd.reason());
        return mapper.toResult(vacationRepo.save(request));
    }
}