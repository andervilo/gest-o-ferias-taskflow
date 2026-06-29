package com.taskflow.gestaocolaboradores.shared.domain;

public class ForbiddenDomainException extends DomainException {
    public ForbiddenDomainException(String message) {
        super(message);
    }
}
