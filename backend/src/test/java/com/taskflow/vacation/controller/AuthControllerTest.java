package com.taskflow.vacation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.dto.LoginRequestDTO;
import com.taskflow.vacation.dto.LoginResponseDTO;
import com.taskflow.vacation.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - Deve autenticar e retornar 200 OK com token JWT")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@taskflow.com", "123456");

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("valid-jwt-token")
                .id(1L)
                .name("Admin User")
                .email("admin@taskflow.com")
                .role(Role.ADMIN)
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@taskflow.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 400 Bad Request se corpo da requisição for inválido")
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", ""); // Campos em branco

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}