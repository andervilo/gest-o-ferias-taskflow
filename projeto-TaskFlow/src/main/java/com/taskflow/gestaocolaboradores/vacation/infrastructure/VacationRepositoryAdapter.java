package com.taskflow.gestaocolaboradores.vacation.infrastructure;

import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import com.taskflow.gestaocolaboradores.vacation.domain.*;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VacationRepositoryAdapter implements VacationRequestRepository {

    private final VacationJpaRepository jpaRepository;
    private final VacationMapper mapper;

    @Override
    public VacationRequest save(VacationRequest request) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(request)));
    }

    @Override
    public Optional<VacationRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<VacationRequest> findAll(VacationFilter filter, int page, int size,
                                                String sortBy, String sortDir) {
        var direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.by(Sort.Direction.DESC, "createdAt");
        var pageable = PageRequest.of(page, size, sort);
        var spec = VacationSpecifications.from(filter);
        var result = jpaRepository.findAll(spec, pageable);
        return new PagedResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()
        );
    }

    @Override
    public List<VacationPeriod> findApprovedPeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                                     LocalDate from, LocalDate to) {
        return jpaRepository
                .findByStatusAndEmployeeIdNotAndPeriodOverlap(VacationStatus.APPROVED, excludeEmployeeId, from, to)
                .stream()
                .map(e -> new VacationPeriod(e.getStartDate(), e.getEndDate()))
                .toList();
    }

    @Override
    public List<VacationPeriod> findActivePeriodsExcludingEmployee(UUID excludeEmployeeId,
                                                                   LocalDate from, LocalDate to) {
        return jpaRepository
                .findByStatusInAndEmployeeIdNotAndPeriodOverlap(
                        EnumSet.of(VacationStatus.PENDING, VacationStatus.APPROVED),
                        excludeEmployeeId, from, to)
                .stream()
                .map(e -> new VacationPeriod(e.getStartDate(), e.getEndDate()))
                .toList();
    }

    @Override
    public List<VacationRequest> findApprovedInPeriod(LocalDate from, LocalDate to,
                                                      List<UUID> scopeEmployeeIds) {
        List<VacationJpaEntity> entities;
        if (scopeEmployeeIds == null) {
            entities = jpaRepository.findByStatusAndPeriodOverlap(VacationStatus.APPROVED, from, to);
        } else if (scopeEmployeeIds.isEmpty()) {
            return List.of();
        } else {
            entities = jpaRepository.findByStatusAndEmployeeIdInAndPeriodOverlap(
                    VacationStatus.APPROVED, scopeEmployeeIds, from, to);
        }
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<VacationRequest> findActiveInPeriod(LocalDate from, LocalDate to,
                                                    List<UUID> scopeEmployeeIds) {
        var statuses = EnumSet.of(VacationStatus.PENDING, VacationStatus.APPROVED);
        List<VacationJpaEntity> entities;
        if (scopeEmployeeIds == null) {
            entities = jpaRepository.findByStatusInAndPeriodOverlap(statuses, from, to);
        } else if (scopeEmployeeIds.isEmpty()) {
            return List.of();
        } else {
            entities = jpaRepository.findByStatusInAndEmployeeIdInAndPeriodOverlap(
                    statuses, scopeEmployeeIds, from, to);
        }
        return entities.stream().map(mapper::toDomain).toList();
    }
}
