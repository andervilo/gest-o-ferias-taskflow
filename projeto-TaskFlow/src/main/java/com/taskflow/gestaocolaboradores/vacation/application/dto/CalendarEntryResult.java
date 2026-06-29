package com.taskflow.gestaocolaboradores.vacation.application.dto;

import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;

import java.time.LocalDate;
import java.util.UUID;

public record CalendarEntryResult(
        UUID id,
        UUID employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        VacationStatus status
) {}