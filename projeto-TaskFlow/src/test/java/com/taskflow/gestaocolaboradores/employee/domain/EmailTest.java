package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void validEmail() {
        assertDoesNotThrow(() -> new Email("user@example.com"));
    }

    @Test
    void emailIsNormalized() {
        Email email = new Email("  USER@Example.COM  ");
        assertEquals("user@example.com", email.value());
    }

    @Test
    void nullEmailThrows() {
        assertThrows(ValidationDomainException.class, () -> new Email(null));
    }

    @Test
    void blankEmailThrows() {
        assertThrows(ValidationDomainException.class, () -> new Email("   "));
    }

    @Test
    void invalidFormatThrows() {
        assertThrows(ValidationDomainException.class, () -> new Email("not-an-email"));
    }

    @Test
    void missingDomainThrows() {
        assertThrows(ValidationDomainException.class, () -> new Email("user@"));
    }
}