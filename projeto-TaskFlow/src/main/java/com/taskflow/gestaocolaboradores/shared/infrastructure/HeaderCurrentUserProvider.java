package com.taskflow.gestaocolaboradores.shared.infrastructure;

import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

// Fase 1: stub baseado em headers — desativado na Fase 2 em favor de JwtCurrentUserProvider.
// Mantido como referência; remova @Component para desativar.
@RequiredArgsConstructor
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    private final HttpServletRequest request;

    @Override
    public CurrentUser getCurrentUser() {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        if (userId == null || userRole == null) {
            throw new ForbiddenDomainException(
                    "Usuário não identificado. Envie os headers X-User-Id e X-User-Role.");
        }
        try {
            return new CurrentUser(UUID.fromString(userId), Role.valueOf(userRole.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ForbiddenDomainException("Header de usuário inválido: " + e.getMessage());
        }
    }
}
