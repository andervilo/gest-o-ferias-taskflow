package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;

public record Email(String value) {

    private static final String PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public Email {
        if (value == null || value.isBlank()) {
            throw new ValidationDomainException("E-mail não pode ser vazio.");
        }
        value = value.trim().toLowerCase();
        if (!value.matches(PATTERN)) {
            throw new ValidationDomainException("E-mail inválido: " + value);
        }
    }
}
