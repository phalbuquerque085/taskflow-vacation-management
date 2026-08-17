package com.taskflow.vacation.dto;

import com.taskflow.vacation.domain.entity.VacationRequest;
import com.taskflow.vacation.domain.enums.VacationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long managerId;
    private String managerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private VacationStatus status;

    public static VacationResponseDTO fromEntity(VacationRequest request) {
        return VacationResponseDTO.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userName(request.getUser().getName())
                .userEmail(request.getUser().getEmail())
                .managerId(request.getUser().getManager() != null ? request.getUser().getManager().getId() : null)
                .managerName(request.getUser().getManager() != null ? request.getUser().getManager().getName() : null)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();
    }
}