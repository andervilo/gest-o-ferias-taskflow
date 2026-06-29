package com.taskflow.gestaocolaboradores.employee.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateEmployeeRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150)
        String name,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        UUID managerId
) {}