package com.taskflow.gestaocolaboradores.shared.domain;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
