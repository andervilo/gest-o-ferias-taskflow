package com.taskflow.gestaocolaboradores.vacation.infrastructure;

import com.taskflow.gestaocolaboradores.vacation.domain.VacationFilter;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class VacationSpecifications {

    static Specification<VacationJpaEntity> from(VacationFilter filter) {
        Specification<VacationJpaEntity> spec = (root, q, cb) -> cb.conjunction();

        if (filter.employeeIds() != null) {
            if (filter.employeeIds().isEmpty()) {
                // scopo vazio: retorna nenhum resultado
                spec = spec.and(inEmployees(List.of()));
            } else {
                spec = spec.and(inEmployees(filter.employeeIds()));
            }
        }
        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            spec = spec.and(hasStatuses(filter.statuses()));
        }
        if (filter.from() != null) {
            spec = spec.and(endsOnOrAfter(filter.from()));
        }
        if (filter.to() != null) {
            spec = spec.and(startsOnOrBefore(filter.to()));
        }
        return spec;
    }

    private static Specification<VacationJpaEntity> inEmployees(List<UUID> ids) {
        return (root, query, cb) -> ids.isEmpty()
                ? cb.disjunction()
                : root.get("employeeId").in(ids);
    }

    private static Specification<VacationJpaEntity> hasStatuses(List<VacationStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    private static Specification<VacationJpaEntity> endsOnOrAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), date);
    }

    private static Specification<VacationJpaEntity> startsOnOrBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), date);
    }
}
