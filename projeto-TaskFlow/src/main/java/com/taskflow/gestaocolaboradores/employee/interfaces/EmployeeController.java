package com.taskflow.gestaocolaboradores.employee.interfaces;

import com.taskflow.gestaocolaboradores.employee.application.*;
import com.taskflow.gestaocolaboradores.employee.application.dto.CreateEmployeeCommand;
import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.application.dto.UpdateEmployeeCommand;
import com.taskflow.gestaocolaboradores.employee.domain.EmployeeFilter;
import com.taskflow.gestaocolaboradores.employee.interfaces.dto.CreateEmployeeRequest;
import com.taskflow.gestaocolaboradores.employee.interfaces.dto.UpdateEmployeeRequest;
import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Colaboradores", description = "Gerenciamento de colaboradores (apenas Admin)")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final CreateEmployeeUseCase createUseCase;
    private final UpdateEmployeeUseCase updateUseCase;
    private final DeleteEmployeeUseCase deleteUseCase;
    private final GetEmployeeUseCase getUseCase;
    private final ListEmployeesUseCase listUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar colaborador (Admin)")
    public EmployeeResult create(@Valid @RequestBody CreateEmployeeRequest req) {
        return createUseCase.execute(new CreateEmployeeCommand(
                req.name(), req.email(), req.password(), req.role(), req.managerId()));
    }

    @GetMapping
    @Operation(summary = "Listar colaboradores com paginação e filtros (Admin, Manager)")
    public PagedResult<EmployeeResult> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UUID managerId) {
        return listUseCase.execute(new EmployeeFilter(q, role, managerId, false), page, size, sort, dir);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador por ID")
    public EmployeeResult get(@PathVariable UUID id) {
        return getUseCase.execute(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar colaborador (Admin)")
    public EmployeeResult update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateEmployeeRequest req) {
        return updateUseCase.execute(new UpdateEmployeeCommand(id, req.name(), req.email(), req.managerId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover colaborador — soft delete (Admin)")
    public void delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
    }
}
