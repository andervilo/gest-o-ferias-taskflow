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

    
    List<VacationPeriod> findApprovedPeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                              LocalDate from, LocalDate to);

    
    List<VacationPeriod> findActivePeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                            LocalDate from, LocalDate to);

    
    List<VacationRequest> findApprovedInPeriod(LocalDate from, LocalDate to,
                                               List<UUID> scopeEmployeeIds);

    
    List<VacationRequest> findActiveInPeriod(LocalDate from, LocalDate to,
                                             List<UUID> scopeEmployeeIds);
}