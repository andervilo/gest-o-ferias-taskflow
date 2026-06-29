package com.taskflow.gestaocolaboradores.vacation.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationRequest(
        UUID employeeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {}