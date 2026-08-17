package com.taskflow.vacation.dto;

import com.taskflow.vacation.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private String token;
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long managerId;
    private String managerName;
}