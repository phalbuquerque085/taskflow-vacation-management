package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        user = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@taskflow.com")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido para o usuário")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length, "O token JWT deve conter header, payload e signature");
    }
}