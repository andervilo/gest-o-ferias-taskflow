package com.taskflow.gestaocolaboradores.vacation.application;

import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.NotFoundException;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequestRepository;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelVacationUseCase {

    private final VacationRequestRepository vacationRepo;
    private final CurrentUserProvider currentUserProvider;
    private final VacationMapper mapper;

    @Transactional
    public VacationResult execute(UUID id) {
        var current = currentUserProvider.getCurrentUser();
        var request = vacationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido de férias não encontrado: " + id));

        if (!current.isAdmin() && !request.getEmployeeId().equals(current.id())) {
            throw new ForbiddenDomainException("Você não tem permissão para cancelar este pedido.");
        }

        request.cancel();
        return mapper.toResult(vacationRepo.save(request));
    }
}