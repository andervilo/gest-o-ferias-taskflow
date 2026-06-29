package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById(UUID id);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    PagedResult<Employee> findAll(EmployeeFilter filter, int page, int size, String sortBy, String sortDir);
    List<Employee> findByManagerId(UUID managerId);
    List<Employee> findAllById(java.util.Collection<UUID> ids);
    List<Employee> findByNameContainingIgnoreCase(String name);
}