package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.dto.LoginRequestDTO;
import com.taskflow.vacation.dto.LoginResponseDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas."));

        boolean matches = user.getPassword().startsWith("$2a$")
                ? passwordEncoder.matches(dto.getPassword(), user.getPassword())
                : user.getPassword().equals(dto.getPassword());

        if (!matches) {
            throw new BusinessException("Credenciais inválidas.");
        }

        String token = jwtService.generateToken(user);

        return LoginResponseDTO.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getName() : null)
                .build();
    }
}