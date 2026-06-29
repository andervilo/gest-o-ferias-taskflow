package com.taskflow.gestaocolaboradores.shared.web;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldError> details
) {
    public record FieldError(String field, String message) {}
}
