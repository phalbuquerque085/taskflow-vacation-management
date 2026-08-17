package com.taskflow.vacation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.vacation.domain.enums.VacationStatus;
import com.taskflow.vacation.dto.VacationCreateRequestDTO;
import com.taskflow.vacation.dto.VacationResponseDTO;
import com.taskflow.vacation.dto.VacationStatusUpdateDTO;
import com.taskflow.vacation.service.VacationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VacationRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class VacationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VacationService vacationService;

    @Test
    @DisplayName("GET /api/vacations - Deve retornar listagem paginada com 200 OK")
    void shouldListVacationsPaged() throws Exception {
        VacationResponseDTO dto = VacationResponseDTO.builder()
                .id(10L)
                .userId(3L)
                .userName("Ana Dev")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(10))
                .status(VacationStatus.PENDING)
                .build();

        when(vacationService.findAllPaged(eq(1L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/vacations")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].userName").value("Ana Dev"));
    }

    @Test
    @DisplayName("POST /api/vacations - Deve criar solicitação de férias e retornar 201 Created")
    void shouldCreateVacation() throws Exception {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(15);
        VacationCreateRequestDTO request = new VacationCreateRequestDTO(3L, start, end);

        VacationResponseDTO response = VacationResponseDTO.builder()
                .id(10L)
                .userId(3L)
                .userName("Ana Dev")
                .startDate(start)
                .endDate(end)
                .status(VacationStatus.PENDING)
                .build();

        when(vacationService.create(any(VacationCreateRequestDTO.class), eq(3L))).thenReturn(response);

        mockMvc.perform(post("/api/vacations")
                        .header("X-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("PATCH /api/vacations/{id}/status - Deve alterar status para APPROVED e retornar 200 OK")
    void shouldUpdateVacationStatus() throws Exception {
        VacationStatusUpdateDTO request = new VacationStatusUpdateDTO(VacationStatus.APPROVED);

        VacationResponseDTO response = VacationResponseDTO.builder()
                .id(10L)
                .status(VacationStatus.APPROVED)
                .build();

        when(vacationService.updateStatus(eq(10L), any(VacationStatusUpdateDTO.class), eq(2L))).thenReturn(response);

        mockMvc.perform(patch("/api/vacations/10/status")
                        .header("X-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("DELETE /api/vacations/{id} - Deve cancelar férias e retornar 204 No Content")
    void shouldCancelVacation() throws Exception {
        doNothing().when(vacationService).cancel(eq(10L), eq(3L));

        mockMvc.perform(delete("/api/vacations/10")
                        .header("X-User-Id", 3L))
                .andExpect(status().isNoContent());
    }
}