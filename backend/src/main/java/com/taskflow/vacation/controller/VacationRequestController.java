package com.taskflow.vacation.controller;

import com.taskflow.vacation.domain.enums.VacationStatus;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<VacationResponseDTO>> findAll(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestParam(required = false) VacationStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort sortOrder = org.springframework.data.domain.Sort.by(
                sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortParams[0]
        );
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortOrder);

        org.springframework.data.domain.Page<VacationResponseDTO> response = vacationService.findAllPaged(requesterId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }
}