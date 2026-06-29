package com.taskflow.gestaocolaboradores.vacation.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateVacationRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {}