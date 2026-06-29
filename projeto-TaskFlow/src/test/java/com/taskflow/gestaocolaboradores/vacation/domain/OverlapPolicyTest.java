package com.taskflow.gestaocolaboradores.vacation.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverlapPolicyTest {

    private final OverlapPolicy policy = new OverlapPolicy();

    private VacationPeriod period(int startDay, int endDay) {
        return new VacationPeriod(
                LocalDate.of(2026, 8, startDay),
                LocalDate.of(2026, 8, endDay));
    }

    @Test
    void noExisting_alwaysAllowed() {
        assertThat(policy.overlapsAny(period(1, 5), List.of())).isFalse();
    }

    @Test
    void candidateOverlapsOne_returnsTrue() {
        assertThat(policy.overlapsAny(period(1, 5), List.of(period(3, 7)))).isTrue();
    }

    @Test
    void candidateAfterAll_returnsFalse() {
        assertThat(policy.overlapsAny(period(10, 15), List.of(period(1, 5), period(6, 9)))).isFalse();
    }

    @Test
    void candidateBeforeAll_returnsFalse() {
        assertThat(policy.overlapsAny(period(1, 3), List.of(period(5, 9), period(11, 15)))).isFalse();
    }

    @Test
    void candidateExactlyMatchesExisting_returnsTrue() {
        assertThat(policy.overlapsAny(period(1, 5), List.of(period(1, 5)))).isTrue();
    }

    @Test
    void multipleExistingNoneOverlap_returnsFalse() {
        assertThat(policy.overlapsAny(period(10, 12),
                List.of(period(1, 3), period(4, 7), period(14, 20)))).isFalse();
    }
}
