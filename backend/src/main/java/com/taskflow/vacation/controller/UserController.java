package com.taskflow.vacation.controller;

import com.taskflow.vacation.dto.UserRequestDTO;
import com.taskflow.vacation.dto.UserResponseDTO;
import com.taskflow.vacation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users or filter by manager")
    public ResponseEntity<List<UserResponseDTO>> getAll(@RequestParam(required = false) Long managerId) {
        if (managerId != null) {
            return ResponseEntity.ok(userService.findByManager(managerId));
        }
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create user (Admin only)")
    public ResponseEntity<UserResponseDTO> create(
            @Valid @RequestBody UserRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto, requesterId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user (Admin only)")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(userService.update(id, dto, requesterId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        userService.delete(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}