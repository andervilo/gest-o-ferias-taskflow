package com.taskflow.gestaocolaboradores.shared.domain;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}