package com.taskflow.gestaocolaboradores.employee.domain;

import com.taskflow.gestaocolaboradores.shared.domain.Role;
import com.taskflow.gestaocolaboradores.shared.domain.ValidationDomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private static final UUID MANAGER_ID = UUID.randomUUID();

    @Test
    void createAdminWithoutManager() {
        assertDoesNotThrow(() -> Employee.create("Ana Admin", "ana@x.com", Role.ADMIN, null, "hash"));
    }

    @Test
    void createManagerWithoutManager() {
        assertDoesNotThrow(() -> Employee.create("Marcos", "marcos@x.com", Role.MANAGER, null, "hash"));
    }

    @Test
    void createCollaboratorWithManager() {
        assertDoesNotThrow(() -> Employee.create("Carla", "carla@x.com", Role.COLLABORATOR, MANAGER_ID, "hash"));
    }

    @Test
    void collaboratorWithoutManagerThrows() {
        assertThrows(ValidationDomainException.class,
                () -> Employee.create("Carla", "carla@x.com", Role.COLLABORATOR, null, "hash"));
    }

    @Test
    void adminWithManagerThrows() {
        assertThrows(ValidationDomainException.class,
                () -> Employee.create("Ana", "ana@x.com", Role.ADMIN, MANAGER_ID, "hash"));
    }

    @Test
    void blankNameThrows() {
        assertThrows(ValidationDomainException.class,
                () -> Employee.create("", "ana@x.com", Role.ADMIN, null, "hash"));
    }

    @Test
    void deactivateSetsActiveFalse() {
        Employee e = Employee.create("Carla", "carla@x.com", Role.COLLABORATOR, MANAGER_ID, "hash");
        assertTrue(e.isActive());
        e.deactivate();
        assertFalse(e.isActive());
    }

    @Test
    void updateChangesFields() {
        UUID newManager = UUID.randomUUID();
        Employee e = Employee.create("Carla", "carla@x.com", Role.COLLABORATOR, MANAGER_ID, "hash");
        e.update("Carla Editada", "carla2@x.com", newManager);
        assertEquals("Carla Editada", e.getName());
        assertEquals("carla2@x.com", e.getEmail().value());
        assertEquals(newManager, e.getManagerId());
    }

    @Test
    void idIsGeneratedOnCreate() {
        Employee e = Employee.create("A", "a@x.com", Role.ADMIN, null, "hash");
        assertNotNull(e.getId());
    }
}