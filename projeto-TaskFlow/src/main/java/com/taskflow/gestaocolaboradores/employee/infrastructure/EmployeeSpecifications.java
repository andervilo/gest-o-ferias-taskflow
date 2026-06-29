package com.taskflow.gestaocolaboradores.employee.infrastructure;

import com.taskflow.gestaocolaboradores.employee.domain.EmployeeFilter;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {}

    public static Specification<EmployeeJpaEntity> from(EmployeeFilter filter) {
        Specification<EmployeeJpaEntity> spec = (root, q, cb) -> cb.conjunction();
        if (!filter.includeInactive()) {
            spec = spec.and(isActive());
        }
        if (filter.role() != null) {
            spec = spec.and(hasRole(filter.role()));
        }
        if (filter.managerId() != null) {
            spec = spec.and(hasManager(filter.managerId()));
        }
        if (filter.query() != null && !filter.query().isBlank()) {
            spec = spec.and(matchesQuery(filter.query()));
        }
        return spec;
    }

    private static Specification<EmployeeJpaEntity> isActive() {
        return (root, q, cb) -> cb.isTrue(root.get("active"));
    }

    private static Specification<EmployeeJpaEntity> hasRole(Role role) {
        return (root, q, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<EmployeeJpaEntity> hasManager(UUID managerId) {
        return (root, q, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    private static Specification<EmployeeJpaEntity> matchesQuery(String query) {
        String like = "%" + query.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("email")), like)
        );
    }
}
