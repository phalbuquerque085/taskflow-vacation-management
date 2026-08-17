package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.entity.VacationRequest;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.domain.enums.VacationStatus;
import com.taskflow.vacation.dto.VacationCreateRequestDTO;
import com.taskflow.vacation.dto.VacationResponseDTO;
import com.taskflow.vacation.dto.VacationStatusUpdateDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.exception.UnauthorizedOperationException;
import com.taskflow.vacation.repository.UserRepository;
import com.taskflow.vacation.repository.VacationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRequestRepository vacationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VacationService vacationService;

    private User admin;
    private User manager;
    private User collaborator;
    private User otherCollaborator;

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
                .name("Ana Dev")
                .email("ana@taskflow.com")
                .role(Role.COLLABORATOR)
                .manager(manager)
                .build();

        otherCollaborator = User.builder()
                .id(4L)
                .name("Bruno Dev")
                .email("bruno@taskflow.com")
                .role(Role.COLLABORATOR)
                .manager(null)
                .build();
    }

    @Nested
    @DisplayName("Criação de Férias")
    class CreateVacationTests {

        @Test
        @DisplayName("Deve solicitar férias com sucesso quando dados e período forem válidos")
        void shouldCreateVacationSuccessfully() {
            LocalDate start = LocalDate.now().plusDays(5);
            LocalDate end = LocalDate.now().plusDays(15);
            VacationCreateRequestDTO dto = new VacationCreateRequestDTO(collaborator.getId(), start, end);

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(vacationRepository.existsOverlappingVacation(eq(start), eq(end), any(), isNull())).thenReturn(false);

            VacationRequest saved = VacationRequest.builder()
                    .id(100L)
                    .user(collaborator)
                    .startDate(start)
                    .endDate(end)
                    .status(VacationStatus.PENDING)
                    .build();

            when(vacationRepository.save(any(VacationRequest.class))).thenReturn(saved);

            VacationResponseDTO response = vacationService.create(dto, collaborator.getId());

            assertNotNull(response);
            assertEquals(100L, response.getId());
            assertEquals(VacationStatus.PENDING, response.getStatus());
            verify(vacationRepository, times(1)).save(any(VacationRequest.class));
        }

        @Test
        @DisplayName("Deve lançar erro quando colaborador tenta solicitar férias para outro colaborador")
        void shouldThrowExceptionWhenCollaboratorRequestsForAnotherUser() {
            VacationCreateRequestDTO dto = new VacationCreateRequestDTO(otherCollaborator.getId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(userRepository.findById(otherCollaborator.getId())).thenReturn(Optional.of(otherCollaborator));

            assertThrows(UnauthorizedOperationException.class, () ->
                    vacationService.create(dto, collaborator.getId())
            );
            verify(vacationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro quando a data de início for posterior à data de fim")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            LocalDate start = LocalDate.now().plusDays(10);
            LocalDate end = LocalDate.now().plusDays(5);
            VacationCreateRequestDTO dto = new VacationCreateRequestDTO(collaborator.getId(), start, end);

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));

            assertThrows(BusinessException.class, () ->
                    vacationService.create(dto, collaborator.getId())
            );
            verify(vacationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro quando houver sobreposição com outras férias na empresa")
        void shouldThrowExceptionWhenVacationOverlaps() {
            LocalDate start = LocalDate.now().plusDays(1);
            LocalDate end = LocalDate.now().plusDays(10);
            VacationCreateRequestDTO dto = new VacationCreateRequestDTO(collaborator.getId(), start, end);

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(vacationRepository.existsOverlappingVacation(eq(start), eq(end), any(), isNull())).thenReturn(true);

            assertThrows(BusinessException.class, () ->
                    vacationService.create(dto, collaborator.getId())
            );
            verify(vacationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Aprovação e Rejeição de Férias")
    class ApprovalTests {

        @Test
        @DisplayName("Gestor deve conseguir aprovar férias de seu colaborador")
        void shouldAllowManagerToApproveDirectSubordinate() {
            VacationRequest vacation = VacationRequest.builder()
                    .id(10L)
                    .user(collaborator)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(5))
                    .status(VacationStatus.PENDING)
                    .build();

            VacationStatusUpdateDTO dto = new VacationStatusUpdateDTO(VacationStatus.APPROVED);

            when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
            when(vacationRepository.findById(10L)).thenReturn(Optional.of(vacation));
            when(vacationRepository.save(any(VacationRequest.class))).thenAnswer(i -> i.getArguments()[0]);

            VacationResponseDTO result = vacationService.updateStatus(10L, dto, manager.getId());

            assertEquals(VacationStatus.APPROVED, result.getStatus());
        }

        @Test
        @DisplayName("Gestor NÃO deve conseguir aprovar férias de colaborador de outro gestor")
        void shouldNotAllowManagerToApproveUnrelatedCollaborator() {
            VacationRequest vacation = VacationRequest.builder()
                    .id(11L)
                    .user(otherCollaborator) // sem vinculo com o manager
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(5))
                    .status(VacationStatus.PENDING)
                    .build();

            VacationStatusUpdateDTO dto = new VacationStatusUpdateDTO(VacationStatus.APPROVED);

            when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
            when(vacationRepository.findById(11L)).thenReturn(Optional.of(vacation));

            assertThrows(UnauthorizedOperationException.class, () ->
                    vacationService.updateStatus(11L, dto, manager.getId())
            );
        }

        @Test
        @DisplayName("Colaborador NÃO pode aprovar férias")
        void shouldNotAllowCollaboratorToApproveVacations() {
            VacationRequest vacation = VacationRequest.builder()
                    .id(12L)
                    .user(collaborator)
                    .status(VacationStatus.PENDING)
                    .build();

            VacationStatusUpdateDTO dto = new VacationStatusUpdateDTO(VacationStatus.APPROVED);

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(vacationRepository.findById(12L)).thenReturn(Optional.of(vacation));

            assertThrows(UnauthorizedOperationException.class, () ->
                    vacationService.updateStatus(12L, dto, collaborator.getId())
            );
        }
    }

    @Nested
    @DisplayName("Cancelamento de Férias")
    class CancelTests {

        @Test
        @DisplayName("Colaborador pode cancelar seu próprio pedido pendente")
        void shouldAllowCollaboratorToCancelOwnPendingVacation() {
            VacationRequest vacation = VacationRequest.builder()
                    .id(20L)
                    .user(collaborator)
                    .status(VacationStatus.PENDING)
                    .build();

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(vacationRepository.findById(20L)).thenReturn(Optional.of(vacation));

            vacationService.cancel(20L, collaborator.getId());

            assertEquals(VacationStatus.CANCELLED, vacation.getStatus());
            verify(vacationRepository, times(1)).save(vacation);
        }

        @Test
        @DisplayName("Colaborador NÃO pode cancelar pedido já finalizado/rejeitado")
        void shouldNotAllowCancelAlreadyFinishedVacation() {
            VacationRequest vacation = VacationRequest.builder()
                    .id(21L)
                    .user(collaborator)
                    .status(VacationStatus.REJECTED)
                    .build();

            when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
            when(vacationRepository.findById(21L)).thenReturn(Optional.of(vacation));

            assertThrows(BusinessException.class, () ->
                    vacationService.cancel(21L, collaborator.getId())
            );
        }
    }
}