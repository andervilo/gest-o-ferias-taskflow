package com.taskflow.gestaocolaboradores.employee.application.dto;

import java.util.UUID;

public record UpdateEmployeeCommand(
        UUID id,
        String name,
        String email,
        UUID managerId
) {}