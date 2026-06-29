package com.taskflow.gestaocolaboradores.employee.application.dto;

import com.taskflow.gestaocolaboradores.shared.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record EmployeeResult(
        UUID id,
        String name,
        String email,
        Role role,
        UUID managerId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
