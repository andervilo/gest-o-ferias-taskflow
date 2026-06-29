package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.Role;

import java.util.UUID;

public record EmployeeFilter(
        String query,
        Role role,
        UUID managerId,
        boolean includeInactive
) {}