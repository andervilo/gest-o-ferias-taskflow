package com.taskflow.gestaocolaboradores.employee.application.dto;

import com.taskflow.gestaocolaboradores.shared.domain.Role;

import java.util.UUID;

public record CreateEmployeeCommand(
        String name,
        String email,
        String password,
        Role role,
        UUID managerId
) {}