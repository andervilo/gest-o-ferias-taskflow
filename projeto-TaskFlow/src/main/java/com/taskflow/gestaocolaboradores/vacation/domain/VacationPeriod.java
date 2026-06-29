package com.taskflow.gestaocolaboradores.vacation.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;

import java.time.LocalDate;

public record VacationPeriod(LocalDate startDate, LocalDate endDate) {

    public VacationPeriod {
        if (startDate == null) throw new ValidationDomainException("startDate é obrigatório.");
        if (endDate == null) throw new ValidationDomainException("endDate é obrigatório.");
        if (endDate.isBefore(startDate))
            throw new ValidationDomainException("endDate deve ser igual ou posterior a startDate.");
    }

    
    public boolean overlapsWith(VacationPeriod other) {
        return !this.startDate.isAfter(other.endDate) && !other.startDate.isAfter(this.endDate);
    }
}