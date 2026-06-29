package com.taskflow.gestaocolaboradores.vacation.domain;

import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacationRequestRepository {

    VacationRequest save(VacationRequest request);

    Optional<VacationRequest> findById(UUID id);

    PagedResult<VacationRequest> findAll(VacationFilter filter, int page, int size,
                                         String sortBy, String sortDir);

    /**
     * Retorna períodos APROVADOS de outros colaboradores que se sobrepõem ao intervalo dado.
     * Usado pela OverlapPolicy antes de aprovar um pedido.
     */
    List<VacationPeriod> findApprovedPeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                              LocalDate from, LocalDate to);

    /**
     * Retorna períodos ATIVOS (PENDING + APPROVED) de outros colaboradores que se sobrepõem ao intervalo.
     * Usado pela OverlapPolicy antes de criar um novo pedido.
     */
    List<VacationPeriod> findActivePeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                            LocalDate from, LocalDate to);

    /** Pedidos aprovados no intervalo (para o endpoint de calendário). */
    List<VacationRequest> findApprovedInPeriod(LocalDate from, LocalDate to,
                                               List<UUID> scopeEmployeeIds);

    /** Pedidos ATIVOS (PENDING + APPROVED) no intervalo (para o calendário completo). */
    List<VacationRequest> findActiveInPeriod(LocalDate from, LocalDate to,
                                             List<UUID> scopeEmployeeIds);
}
