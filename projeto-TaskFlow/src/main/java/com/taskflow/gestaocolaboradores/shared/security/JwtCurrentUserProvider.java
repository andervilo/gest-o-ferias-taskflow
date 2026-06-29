package com.taskflow.gestaocolaboradores.shared.security;

import com.taskflow.gestaocolaboradores.shared.application.CurrentUser;
import com.taskflow.gestaocolaboradores.shared.application.CurrentUserProvider;
import com.taskflow.gestaocolaboradores.shared.domain.ForbiddenDomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenDomainException("Não autenticado.");
        }
        return new CurrentUser(principal.getId(), principal.getRole());
    }
}