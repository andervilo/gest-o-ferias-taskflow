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

    /** Verifica sobreposição inclusiva: [a1,a2] ∩ [b1,b2] ≠ ∅ ↔ a1<=b2 AND b1<=a2 */
    public boolean overlapsWith(VacationPeriod other) {
        return !this.startDate.isAfter(other.endDate) && !other.startDate.isAfter(this.endDate);
    }
}
