package com.taskflow.gestaocolaboradores.vacation.domain;

import java.util.List;

public class OverlapPolicy {

    public boolean overlapsAny(VacationPeriod candidate, List<VacationPeriod> existing) {
        return existing.stream().anyMatch(candidate::overlapsWith);
    }
}