package com.taskflow.gestaocolaboradores.shared.application;

import com.taskflow.gestaocolaboradores.shared.domain.Role;

import java.util.UUID;

public record CurrentUser(UUID id, Role role) {
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isManager() { return role == Role.MANAGER; }
    public boolean isCollaborator() { return role == Role.COLLABORATOR; }
}
