package com.taskflow.gestaocolaboradores.employee.infrastructure;

import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeFilter;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeRepository;
import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryAdapter implements EmployeeRepository {

    private final EmployeeJpaRepository jpaRepository;
    private final EmployeeMapper mapper;

    @Override
    public Employee save(Employee employee) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(employee)));
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return jpaRepository.findByEmail(email.trim().toLowerCase()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public PagedResult<Employee> findAll(EmployeeFilter filter, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<EmployeeJpaEntity> spec = EmployeeSpecifications.from(filter);
        Page<EmployeeJpaEntity> entityPage = jpaRepository.findAll(spec, pageable);
        List<Employee> content = entityPage.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page, size, entityPage.getTotalElements(), entityPage.getTotalPages());
    }

    @Override
    public List<Employee> findByManagerId(UUID managerId) {
        return jpaRepository.findByManagerId(managerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Employee> findAllById(java.util.Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Employee> findByNameContainingIgnoreCase(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name).stream()
                .map(mapper::toDomain).toList();
    }
}