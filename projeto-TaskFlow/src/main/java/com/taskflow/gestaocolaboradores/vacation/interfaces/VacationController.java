package com.taskflow.gestaocolaboradores.vacation.interfaces;

import com.taskflow.gestaocolaboradores.vacation.application.*;
import com.taskflow.gestaocolaboradores.vacation.application.dto.CalendarEntryResult;
import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.application.dto.CreateVacationCommand;
import com.taskflow.gestaocolaboradores.vacation.application.dto.UpdateVacationCommand;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;
import com.taskflow.gestaocolaboradores.vacation.interfaces.dto.CreateVacationRequest;
import com.taskflow.gestaocolaboradores.vacation.interfaces.dto.UpdateVacationRequest;
import com.taskflow.gestaocolaboradores.shared.domain.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "Gerenciamento de pedidos de férias")
@SecurityRequirement(name = "bearerAuth")
public class VacationController {

    private final CreateVacationUseCase createUseCase;
    private final UpdateVacationUseCase updateUseCase;
    private final CancelVacationUseCase cancelUseCase;
    private final ApproveVacationUseCase approveUseCase;
    private final RejectVacationUseCase rejectUseCase;
    private final GetVacationUseCase getUseCase;
    private final ListVacationsUseCase listUseCase;
    private final GetCalendarEntriesUseCase calendarUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar férias")
    public VacationResult create(@RequestBody @Valid CreateVacationRequest req) {
        return createUseCase.execute(new CreateVacationCommand(
                req.employeeId(), req.startDate(), req.endDate(), req.reason()));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos de férias (paginado)")
    public PagedResult<VacationResult> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<VacationStatus> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir
    ) {
        return listUseCase.execute(employeeId, managerId, name, status, from, to, page, size, sort, dir);
    }

    @GetMapping("/calendar")
    @Operation(summary = "Períodos aprovados para visualização em calendário")
    public List<CalendarEntryResult> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return calendarUseCase.execute(from, to);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um pedido")
    public VacationResult get(@PathVariable UUID id) {
        return getUseCase.execute(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar pedido (somente PENDING)")
    public VacationResult update(@PathVariable UUID id,
                                  @RequestBody @Valid UpdateVacationRequest req) {
        return updateUseCase.execute(new UpdateVacationCommand(id, req.startDate(), req.endDate(), req.reason()));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprovar pedido")
    public VacationResult approve(@PathVariable UUID id) {
        return approveUseCase.execute(id);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rejeitar pedido")
    public VacationResult reject(@PathVariable UUID id) {
        return rejectUseCase.execute(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido")
    public VacationResult cancel(@PathVariable UUID id) {
        return cancelUseCase.execute(id);
    }
}