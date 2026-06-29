package com.taskflow.gestaocolaboradores.vacation.application.dto;

import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VacationResult(
        UUID id,
        UUID employeeId,
        String employeeName,
        String managerName,
        LocalDate startDate,
        LocalDate endDate,
        VacationStatus status,
        String reason,
        UUID decidedBy,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {}