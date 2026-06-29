package com.taskflow.gestaocolaboradores.vacation.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class VacationRequest {

    private final UUID id;
    private final UUID employeeId;
    private VacationPeriod period;
    private VacationStatus status;
    private String reason;
    private UUID decidedBy;
    private Instant decidedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private VacationRequest(UUID id, UUID employeeId, VacationPeriod period, VacationStatus status,
                            String reason, UUID decidedBy, Instant decidedAt,
                            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.period = period;
        this.status = status;
        this.reason = reason;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static VacationRequest create(UUID employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        if (startDate != null && startDate.isBefore(LocalDate.now())) {
            throw new ValidationDomainException("startDate não pode ser uma data no passado.");
        }
        var period = new VacationPeriod(startDate, endDate);
        var now = Instant.now();
        return new VacationRequest(UUID.randomUUID(), employeeId, period,
                VacationStatus.PENDING, reason, null, null, now, now);
    }

    public static VacationRequest reconstitute(UUID id, UUID employeeId, LocalDate startDate,
                                               LocalDate endDate, VacationStatus status, String reason,
                                               UUID decidedBy, Instant decidedAt,
                                               Instant createdAt, Instant updatedAt) {
        return new VacationRequest(id, employeeId, new VacationPeriod(startDate, endDate),
                status, reason, decidedBy, decidedAt, createdAt, updatedAt);
    }

    public void update(LocalDate startDate, LocalDate endDate, String reason) {
        requirePending("editar");
        if (startDate != null && startDate.isBefore(LocalDate.now())) {
            throw new ValidationDomainException("startDate não pode ser uma data no passado.");
        }
        this.period = new VacationPeriod(startDate, endDate);
        this.reason = reason;
        this.updatedAt = Instant.now();
    }

    public void approve(UUID decidedById) {
        requirePending("aprovar");
        this.status = VacationStatus.APPROVED;
        this.decidedBy = decidedById;
        this.decidedAt = Instant.now();
        this.updatedAt = this.decidedAt;
    }

    public void reject(UUID decidedById) {
        requirePending("rejeitar");
        this.status = VacationStatus.REJECTED;
        this.decidedBy = decidedById;
        this.decidedAt = Instant.now();
        this.updatedAt = this.decidedAt;
    }

    public void cancel() {
        if (status == VacationStatus.REJECTED || status == VacationStatus.CANCELLED) {
            throw new ConflictException("INVALID_STATE_TRANSITION",
                    "Pedido não pode ser cancelado no estado " + status + ".");
        }
        this.status = VacationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    private void requirePending(String action) {
        if (status != VacationStatus.PENDING) {
            throw new ConflictException("INVALID_STATE_TRANSITION",
                    "Só é possível " + action + " pedidos com status PENDING. Status atual: " + status + ".");
        }
    }
}
