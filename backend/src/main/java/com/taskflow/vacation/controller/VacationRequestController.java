package com.taskflow.vacation.controller;

import com.taskflow.vacation.dto.VacationCreateRequestDTO;
import com.taskflow.vacation.dto.VacationResponseDTO;
import com.taskflow.vacation.dto.VacationStatusUpdateDTO;
import com.taskflow.vacation.dto.VacationUpdateRequestDTO;
import com.taskflow.vacation.service.VacationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Vacations", description = "Endpoints for vacation request management")
public class VacationRequestController {

    private final VacationService vacationService;

    @GetMapping
    @Operation(summary = "List vacation requests based on requester role")
    public ResponseEntity<List<VacationResponseDTO>> getAll(@RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.findAll(requesterId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vacation request details by ID")
    public ResponseEntity<VacationResponseDTO> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.findById(id, requesterId));
    }

    @PostMapping
    @Operation(summary = "Create vacation request")
    public ResponseEntity<VacationResponseDTO> create(
            @Valid @RequestBody VacationCreateRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacationService.create(dto, requesterId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pending vacation request")
    public ResponseEntity<VacationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody VacationUpdateRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.update(id, dto, requesterId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Approve or reject vacation request (Manager or Admin)")
    public ResponseEntity<VacationResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody VacationStatusUpdateDTO dto,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(vacationService.updateStatus(id, dto, requesterId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel vacation request")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        vacationService.cancel(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}