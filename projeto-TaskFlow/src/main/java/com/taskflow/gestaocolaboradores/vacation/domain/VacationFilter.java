package com.taskflow.gestaocolaboradores.vacation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Critérios de busca para pedidos de férias.
 * employeeIds=null → sem filtro por colaborador.
 * employeeIds=lista vazia → retorna nada (scopo vazio).
 * statuses=null ou vazio → sem filtro por status.
 */
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
