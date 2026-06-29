package com.taskflow.gestaocolaboradores.shared.security.dto;

import com.taskflow.gestaocolaboradores.shared.domain.Role;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID id,
        String name,
        String email,
        Role role
) {}
