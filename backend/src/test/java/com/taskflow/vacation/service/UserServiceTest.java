package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.dto.UserRequestDTO;
import com.taskflow.vacation.dto.UserResponseDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.exception.ResourceNotFoundException;
import com.taskflow.vacation.exception.UnauthorizedOperationException;
import com.taskflow.vacation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User admin;
    private User manager;
    private User collaborator;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@taskflow.com")
                .role(Role.ADMIN)
                .build();

        manager = User.builder()
                .id(2L)
                .name("Carlos Manager")
                .email("carlos@taskflow.com")
                .role(Role.MANAGER)
                .build();

        collaborator = User.builder()
                .id(3L)
                .name("Bruna Dev")
                .email("bruna@taskflow.com")
                .role(Role.COLLABORATOR)
                .manager(manager)
                .build();
    }

    @Nested
    @DisplayName("Criação de Usuários")
    class CreateUserTests {

        @Test
        @DisplayName("Admin deve conseguir criar um novo colaborador com sucesso")
        void shouldAllowAdminToCreateCollaborator() {
            UserRequestDTO dto = UserRequestDTO.builder()
                    .name("Novo Colaborador")
                    .email("novo@taskflow.com")
                    .role(Role.COLLABORATOR)
                    .managerId(manager.getId())
                    .build();

            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("novo@taskflow.com")).thenReturn(Optional.empty());
            when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

            User saved = User.builder()
                    .id(10L)
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .role(dto.getRole())
                    .manager(manager)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResponseDTO response = userService.create(dto, admin.getId());

            assertNotNull(response);
            assertEquals("novo@taskflow.com", response.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Não-Admin NÃO deve conseguir criar colaboradores")
        void shouldNotAllowNonAdminToCreateUser() {
            UserRequestDTO dto = UserRequestDTO.builder()
                    .name("Novo")
                    .email("novo@taskflow.com")
                    .role(Role.COLLABORATOR)
                    .build();

            when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

            assertThrows(UnauthorizedOperationException.class, () ->
                    userService.create(dto, manager.getId())
            );
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar cadastrar e-mail já existente")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            UserRequestDTO dto = UserRequestDTO.builder()
                    .name("Duplicado")
                    .email("carlos@taskflow.com")
                    .role(Role.COLLABORATOR)
                    .build();

            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("carlos@taskflow.com")).thenReturn(Optional.of(manager));

            assertThrows(BusinessException.class, () ->
                    userService.create(dto, admin.getId())
            );
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Exclusão de Usuários")
    class DeleteUserTests {

        @Test
        @DisplayName("Admin deve conseguir deletar um colaborador")
        void shouldAllowAdminToDeleteUser() {
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.existsById(collaborator.getId())).thenReturn(true);

            userService.delete(collaborator.getId(), admin.getId());

            verify(userRepository, times(1)).deleteById(collaborator.getId());
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar deletar usuário inexistente")
        void shouldThrowExceptionWhenUserToDeleteDoesNotExist() {
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () ->
                    userService.delete(99L, admin.getId())
            );
            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Manager ou Colaborador NÃO podem deletar usuários")
        void shouldNotAllowNonAdminToDeleteUser() {
            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));

            assertThrows(UnauthorizedOperationException.class, () ->
                    userService.delete(collaborator.getId(), collaborator.getId())
            );
            verify(userRepository, never()).deleteById(any());
        }
    }
}