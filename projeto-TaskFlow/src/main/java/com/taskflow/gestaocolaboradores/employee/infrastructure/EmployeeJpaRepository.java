package com.taskflow.gestaocolaboradores.employee.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeJpaRepository
        extends JpaRepository<EmployeeJpaEntity, UUID>, JpaSpecificationExecutor<EmployeeJpaEntity> {

    Optional<EmployeeJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<EmployeeJpaEntity> findByManagerId(UUID managerId);

    List<EmployeeJpaEntity> findByNameContainingIgnoreCase(String name);
}