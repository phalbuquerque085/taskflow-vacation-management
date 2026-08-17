package com.taskflow.vacation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.dto.UserRequestDTO;
import com.taskflow.vacation.dto.UserResponseDTO;
import com.taskflow.vacation.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users - Deve listar colaboradores e retornar 200 OK")
    void shouldListUsers() throws Exception {
        UserResponseDTO user = UserResponseDTO.builder()
                .id(1L)
                .name("Admin")
                .email("admin@taskflow.com")
                .role(Role.ADMIN)
                .build();

        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Admin"));
    }

    @Test
    @DisplayName("POST /api/users - Deve criar colaborador e retornar 201 Created")
    void shouldCreateUser() throws Exception {
        UserRequestDTO request = UserRequestDTO.builder()
                .name("Carlos")
                .email("carlos@taskflow.com")
                .role(Role.MANAGER)
                .build();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(2L)
                .name("Carlos")
                .email("carlos@taskflow.com")
                .role(Role.MANAGER)
                .build();

        when(userService.create(any(UserRequestDTO.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("carlos@taskflow.com"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Deve excluir colaborador e retornar 204 No Content")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(eq(2L), eq(1L));

        mockMvc.perform(delete("/api/users/2")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }
}