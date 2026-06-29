package com.taskflow.gestaocolaboradores.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TaskFlow — Sistema de Gestão de Férias",
                version = "1.0",
                description = "API REST para gerenciamento de colaboradores e pedidos de férias. " +
                              "Autentique via POST /api/auth/login e use o token retornado como Bearer."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido via POST /api/auth/login"
)
public class OpenApiConfig {}