package com.taskflow.gestaocolaboradores.employee.interfaces.dto;

import com.taskflow.gestaocolaboradores.shared.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateEmployeeRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150)
        String name,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres.")
        String password,

        @NotNull(message = "Role é obrigatório.")
        Role role,

        UUID managerId
) {}
