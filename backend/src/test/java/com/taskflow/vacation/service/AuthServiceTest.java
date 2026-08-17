package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.dto.LoginRequestDTO;
import com.taskflow.vacation.dto.LoginResponseDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Carlos Manager")
                .email("carlos@taskflow.com")
                .password(encoder.encode("123456"))
                .role(Role.MANAGER)
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais estiverem corretas")
    void shouldAuthenticateSuccessfully() {
        LoginRequestDTO dto = new LoginRequestDTO("carlos@taskflow.com", "123456");

        when(userRepository.findByEmail("carlos@taskflow.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mocked-jwt-token");

        LoginResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("carlos@taskflow.com", response.getEmail());
        assertEquals(Role.MANAGER, response.getRole());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("Deve lançar erro quando o e-mail não for encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequestDTO dto = new LoginRequestDTO("inexistente@taskflow.com", "123456");

        when(userRepository.findByEmail("inexistente@taskflow.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(dto));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando a senha estiver incorreta")
    void shouldThrowExceptionWhenPasswordIsWrong() {
        LoginRequestDTO dto = new LoginRequestDTO("carlos@taskflow.com", "senha_errada");

        when(userRepository.findByEmail("carlos@taskflow.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authService.login(dto));
        verify(jwtService, never()).generateToken(any());
    }
}