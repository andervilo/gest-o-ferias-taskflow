package com.taskflow.gestaocolaboradores.shared.domain;

import lombok.Getter;

@Getter
public class ConflictException extends DomainException {
    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }
}