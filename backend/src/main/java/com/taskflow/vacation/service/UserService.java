package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.dto.UserRequestDTO;
import com.taskflow.vacation.dto.UserResponseDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.exception.ResourceNotFoundException;
import com.taskflow.vacation.exception.UnauthorizedOperationException;
import com.taskflow.vacation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponseDTO.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findByManager(Long managerId) {
        return userRepository.findByManagerId(managerId).stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto, Long requesterId) {
        validateAdminPermission(requesterId);

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered: " + dto.getEmail());
        }

        User manager = null;
        if (dto.getRole() == Role.COLLABORATOR) {
            if (dto.getManagerId() == null) {
                throw new BusinessException("A collaborator must be assigned to a manager");
            }
            manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + dto.getManagerId()));
            if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
                throw new BusinessException("Assigned user is not a manager");
            }
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .manager(manager)
                .build();

        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto, Long requesterId) {
        validateAdminPermission(requesterId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.findByEmail(dto.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Email already in use by another user");
                });

        User manager = null;
        if (dto.getRole() == Role.COLLABORATOR) {
            if (dto.getManagerId() == null) {
                throw new BusinessException("A collaborator must be assigned to a manager");
            }
            manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + dto.getManagerId()));
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setManager(manager);

        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id, Long requesterId) {
        validateAdminPermission(requesterId);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private void validateAdminPermission(Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedOperationException("Requester ID header (X-User-Id) is required");
        }
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester user not found"));
        if (requester.getRole() != Role.ADMIN) {
            throw new UnauthorizedOperationException("Only administrators can manage users");
        }
    }
}