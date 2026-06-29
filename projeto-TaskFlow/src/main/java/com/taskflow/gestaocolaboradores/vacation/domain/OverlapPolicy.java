package com.taskflow.gestaocolaboradores.vacation.domain;

import java.util.List;

/** Domain service: verifica se um período candidato se sobrepõe a outros já aprovados. */
public class OverlapPolicy {

    public boolean overlapsAny(VacationPeriod candidate, List<VacationPeriod> existing) {
        return existing.stream().anyMatch(candidate::overlapsWith);
    }
}
