package com.taskflow.gestaocolaboradores.vacation.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacationPeriodTest {

    private static final LocalDate D1 = LocalDate.of(2026, 8, 1);
    private static final LocalDate D5 = LocalDate.of(2026, 8, 5);
    private static final LocalDate D3 = LocalDate.of(2026, 8, 3);
    private static final LocalDate D7 = LocalDate.of(2026, 8, 7);
    private static final LocalDate D10 = LocalDate.of(2026, 8, 10);

    @Test
    void validPeriod_sameDay_accepted() {
        var p = new VacationPeriod(D1, D1);
        assertThat(p.startDate()).isEqualTo(D1);
        assertThat(p.endDate()).isEqualTo(D1);
    }

    @Test
    void validPeriod_multipledays_accepted() {
        var p = new VacationPeriod(D1, D5);
        assertThat(p.startDate()).isEqualTo(D1);
        assertThat(p.endDate()).isEqualTo(D5);
    }

    @Test
    void invalidPeriod_endBeforeStart_throws() {
        assertThatThrownBy(() -> new VacationPeriod(D5, D1))
                .isInstanceOf(ValidationDomainException.class)
                .hasMessageContaining("endDate deve ser igual ou posterior");
    }

    @Test
    void nullStartDate_throws() {
        assertThatThrownBy(() -> new VacationPeriod(null, D5))
                .isInstanceOf(ValidationDomainException.class);
    }

    @Test
    void nullEndDate_throws() {
        assertThatThrownBy(() -> new VacationPeriod(D1, null))
                .isInstanceOf(ValidationDomainException.class);
    }

    @Test
    void overlaps_partialOverlap_returnsTrue() {
        var a = new VacationPeriod(D1, D5);
        var b = new VacationPeriod(D3, D7);
        assertThat(a.overlapsWith(b)).isTrue();
        assertThat(b.overlapsWith(a)).isTrue();
    }

    @Test
    void overlaps_adjacent_returnsFalse() {
        var a = new VacationPeriod(D1, D5);
        var b = new VacationPeriod(LocalDate.of(2026, 8, 6), D10);
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlaps_noOverlap_returnsFalse() {
        var a = new VacationPeriod(D1, D3);
        var b = new VacationPeriod(D7, D10);
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlaps_identicalPeriods_returnsTrue() {
        var a = new VacationPeriod(D1, D5);
        var b = new VacationPeriod(D1, D5);
        assertThat(a.overlapsWith(b)).isTrue();
    }

    @Test
    void overlaps_touchingAtEndpoint_returnsTrue() {
        var a = new VacationPeriod(D1, D5);
        var b = new VacationPeriod(D5, D10);
        assertThat(a.overlapsWith(b)).isTrue();
    }
}