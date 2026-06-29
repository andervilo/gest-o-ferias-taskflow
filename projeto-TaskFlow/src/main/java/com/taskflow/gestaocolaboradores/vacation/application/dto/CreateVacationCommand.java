package com.taskflow.gestaocolaboradores.vacation.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationCommand(
        UUID employeeId,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {}
