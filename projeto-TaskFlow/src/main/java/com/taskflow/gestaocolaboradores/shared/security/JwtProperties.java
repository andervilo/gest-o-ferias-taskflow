package com.taskflow.gestaocolaboradores.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, int expirationMinutes) {}