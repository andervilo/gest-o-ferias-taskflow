package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.Role;
import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Employee {

    private final UUID id;
    private String name;
    private Email email;
    private final Role role;
    private UUID managerId;
    private String passwordHash;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private Employee(UUID id, String name, Email email, Role role,
                     UUID managerId, String passwordHash, boolean active,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.managerId = managerId;
        this.passwordHash = passwordHash;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Employee create(String name, String emailStr, Role role, UUID managerId, String passwordHash) {
        validate(name, role, managerId);
        return new Employee(UUID.randomUUID(), name, new Email(emailStr), role,
                managerId, passwordHash, true, Instant.now(), Instant.now());
    }

    // Reconstitui do banco sem regras de criação.
    public static Employee reconstitute(UUID id, String name, String emailStr, Role role,
                                         UUID managerId, String passwordHash, boolean active,
                                         Instant createdAt, Instant updatedAt) {
        return new Employee(id, name, new Email(emailStr), role,
                managerId, passwordHash, active, createdAt, updatedAt);
    }

    public void update(String name, String emailStr, UUID managerId) {
        validate(name, this.role, managerId);
        this.name = name;
        this.email = new Email(emailStr);
        this.managerId = managerId;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    private static void validate(String name, Role role, UUID managerId) {
        if (name == null || name.isBlank()) {
            throw new ValidationDomainException("Nome não pode ser vazio.");
        }
        if (role == Role.COLLABORATOR && managerId == null) {
            throw new ValidationDomainException("Colaborador deve estar associado a um manager.");
        }
        if (role != Role.COLLABORATOR && managerId != null) {
            throw new ValidationDomainException("Admin e Manager não podem ter manager associado.");
        }
    }
}
