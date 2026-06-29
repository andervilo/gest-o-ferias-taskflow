package com.taskflow.gestaocolaboradores.vacation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VacationFilter(
        List<UUID> employeeIds,
        List<VacationStatus> statuses,
        LocalDate from,
        LocalDate to
) {
    public static VacationFilter empty() {
        return new VacationFilter(null, null, null, null);
    }
}