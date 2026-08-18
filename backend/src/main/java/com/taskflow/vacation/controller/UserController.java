package com.taskflow.vacation.controller;

import com.taskflow.vacation.dto.UserRequestDTO;
import com.taskflow.vacation.dto.UserResponseDTO;
import com.taskflow.vacation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Colaboradores", description = "Endpoints para gestão de colaboradores e hierarquia da empresa")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar colaboradores", description = "Retorna todos os colaboradores cadastrados ou filtra pelos liderados de um gestor específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<UserResponseDTO>> getAll(
            @Parameter(description = "ID do gestor para filtrar os liderados diretos")
            @RequestParam(required = false) Long managerId) {
        if (managerId != null) {
            return ResponseEntity.ok(userService.findByManager(managerId));
        }
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador por ID", description = "Obtém os detalhes de um colaborador específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador encontrado"),
            @ApiResponse(responseCode = "404", description = "Colaborador não encontrado")
    })
    public ResponseEntity<UserResponseDTO> getById(
            @Parameter(description = "ID do colaborador") @PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo colaborador", description = "Cria um novo usuário no sistema (Exclusivo para perfil Administrador).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Colaborador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<UserResponseDTO> create(
            @Valid @RequestBody UserRequestDTO dto,
            @Parameter(description = "ID do usuário autenticado que está realizando a requisição")
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto, requesterId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar colaborador", description = "Atualiza os dados de um colaborador existente (Exclusivo para perfil Administrador).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Colaborador não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<UserResponseDTO> update(
            @Parameter(description = "ID do colaborador") @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto,
            @Parameter(description = "ID do usuário autenticado que está realizando a requisição")
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(userService.update(id, dto, requesterId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover colaborador", description = "Exclui um colaborador do sistema (Exclusivo para perfil Administrador).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Colaborador removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Colaborador não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do colaborador") @PathVariable Long id,
            @Parameter(description = "ID do usuário autenticado que está realizando a requisição")
            @RequestHeader("X-User-Id") Long requesterId) {
        userService.delete(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}