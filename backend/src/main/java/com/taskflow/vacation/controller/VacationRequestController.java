package com.taskflow.vacation.controller;

import com.taskflow.vacation.domain.enums.VacationStatus;
import com.taskflow.vacation.dto.VacationCreateRequestDTO;
import com.taskflow.vacation.dto.VacationResponseDTO;
import com.taskflow.vacation.dto.VacationStatusUpdateDTO;
import com.taskflow.vacation.dto.VacationUpdateRequestDTO;
import com.taskflow.vacation.service.VacationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Férias", description = "Endpoints para solicitação, aprovação e consulta de agendamento de férias")
public class VacationRequestController {

    private final VacationService vacationService;

    @GetMapping
    @Operation(summary = "Listar solicitações de férias", description = "Consulta paginada de solicitações de férias com filtros por status e intervalo de datas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de solicitações retornada com sucesso")
    })
    public ResponseEntity<Page<VacationResponseDTO>> findAll(
            @Parameter(description = "ID do usuário autenticado")
            @RequestHeader("X-User-Id") Long requesterId,
            @Parameter(description = "Filtrar por status da solicitação (PENDING, APPROVED, REJECTED, CANCELLED)")
            @RequestParam(required = false) VacationStatus status,
            @Parameter(description = "Data de início mínima (formato AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Data de fim máxima (formato AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Número da página (inicia em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Critério de ordenação no formato 'campo,direcao' (ex: startDate,desc)")
            @RequestParam(defaultValue = "startDate,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(
                sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                sortParams[0]
        );
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<VacationResponseDTO> response = vacationService.findAllPaged(requesterId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar solicitação por ID", description = "Retorna os detalhes de uma solicitação de férias específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<VacationResponseDTO> getById(
            @Parameter(description = "ID da solicitação de férias") @PathVariable Long id,
            @Parameter(description = "ID do usuário autenticado") @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.findById(id, requesterId));
    }

    @PostMapping
    @Operation(summary = "Criar solicitação de férias", description = "Cadastra um novo pedido de férias validando a regra de não sobreposição global.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Período inválido ou sobreposição com outro colaborador"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<VacationResponseDTO> create(
            @Valid @RequestBody VacationCreateRequestDTO dto,
            @Parameter(description = "ID do usuário autenticado solicitante")
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacationService.create(dto, requesterId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar solicitação de férias pendente", description = "Permite que o solicitante altere as datas de um pedido que ainda esteja com status Pendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Período conflitante ou status inválido para alteração"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<VacationResponseDTO> update(
            @Parameter(description = "ID da solicitação de férias") @PathVariable Long id,
            @Valid @RequestBody VacationUpdateRequestDTO dto,
            @Parameter(description = "ID do usuário autenticado") @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.update(id, dto, requesterId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Aprovar ou rejeitar solicitação", description = "Atualiza o status para APROVADO ou REJEITADO (Exclusivo para Gestores da equipe ou Administradores).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o usuário autenticado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<VacationResponseDTO> updateStatus(
            @Parameter(description = "ID da solicitação de férias") @PathVariable Long id,
            @Valid @RequestBody VacationStatusUpdateDTO dto,
            @Parameter(description = "ID do gestor ou administrador autenticado")
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.updateStatus(id, dto, requesterId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar solicitação de férias", description = "Cancela uma solicitação pendente realizada pelo próprio colaborador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação cancelada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Solicitação já processada (aprovada/rejeitada) não pode ser cancelada"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<Void> cancel(
            @Parameter(description = "ID da solicitação de férias") @PathVariable Long id,
            @Parameter(description = "ID do colaborador autenticado")
            @RequestHeader("X-User-Id") Long requesterId) {
        vacationService.cancel(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}