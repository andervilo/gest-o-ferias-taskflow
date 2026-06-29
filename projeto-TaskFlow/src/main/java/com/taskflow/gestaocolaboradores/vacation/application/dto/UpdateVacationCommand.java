package com.taskflow.gestaocolaboradores.vacation.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateVacationCommand(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {}