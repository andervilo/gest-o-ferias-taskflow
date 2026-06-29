package com.taskflow.gestaocolaboradores.vacation.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacationRequestTest {

    private static final UUID EMP_ID = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2026, 8, 1);
    private static final LocalDate END = LocalDate.of(2026, 8, 5);

    @Test
    void create_generatesIdAndStatusPending() {
        var req = VacationRequest.create(EMP_ID, START, END, "reason");
        assertThat(req.getId()).isNotNull();
        assertThat(req.getEmployeeId()).isEqualTo(EMP_ID);
        assertThat(req.getStatus()).isEqualTo(VacationStatus.PENDING);
        assertThat(req.getPeriod().startDate()).isEqualTo(START);
        assertThat(req.getPeriod().endDate()).isEqualTo(END);
        assertThat(req.getCreatedAt()).isNotNull();
    }

    @Test
    void approve_fromPending_changesStatus() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.approve(MANAGER_ID);
        assertThat(req.getStatus()).isEqualTo(VacationStatus.APPROVED);
        assertThat(req.getDecidedBy()).isEqualTo(MANAGER_ID);
        assertThat(req.getDecidedAt()).isNotNull();
    }

    @Test
    void reject_fromPending_changesStatus() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.reject(MANAGER_ID);
        assertThat(req.getStatus()).isEqualTo(VacationStatus.REJECTED);
        assertThat(req.getDecidedBy()).isEqualTo(MANAGER_ID);
    }

    @Test
    void approve_fromApproved_throwsConflict() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.approve(MANAGER_ID);
        assertThatThrownBy(() -> req.approve(MANAGER_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void reject_fromRejected_throwsConflict() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.reject(MANAGER_ID);
        assertThatThrownBy(() -> req.reject(MANAGER_ID))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cancel_fromPending_changesStatus() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.cancel();
        assertThat(req.getStatus()).isEqualTo(VacationStatus.CANCELLED);
    }

    @Test
    void cancel_fromApproved_changesStatus() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.approve(MANAGER_ID);
        req.cancel();
        assertThat(req.getStatus()).isEqualTo(VacationStatus.CANCELLED);
    }

    @Test
    void cancel_fromCancelled_throwsConflict() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.cancel();
        assertThatThrownBy(() -> req.cancel())
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    void cancel_fromRejected_throwsConflict() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.reject(MANAGER_ID);
        assertThatThrownBy(() -> req.cancel())
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_fromPending_changesPeriod() {
        var req = VacationRequest.create(EMP_ID, START, END, "old reason");
        var newStart = LocalDate.of(2026, 9, 1);
        var newEnd = LocalDate.of(2026, 9, 10);
        req.update(newStart, newEnd, "new reason");
        assertThat(req.getPeriod().startDate()).isEqualTo(newStart);
        assertThat(req.getReason()).isEqualTo("new reason");
    }

    @Test
    void update_fromApproved_throwsConflict() {
        var req = VacationRequest.create(EMP_ID, START, END, null);
        req.approve(MANAGER_ID);
        assertThatThrownBy(() -> req.update(START, END, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PENDING");
    }
}