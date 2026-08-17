package com.taskflow.vacation.service;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.entity.VacationRequest;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.domain.enums.VacationStatus;
import com.taskflow.vacation.dto.VacationCreateRequestDTO;
import com.taskflow.vacation.dto.VacationResponseDTO;
import com.taskflow.vacation.dto.VacationStatusUpdateDTO;
import com.taskflow.vacation.dto.VacationUpdateRequestDTO;
import com.taskflow.vacation.exception.BusinessException;
import com.taskflow.vacation.exception.ResourceNotFoundException;
import com.taskflow.vacation.exception.UnauthorizedOperationException;
import com.taskflow.vacation.repository.UserRepository;
import com.taskflow.vacation.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRequestRepository vacationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<VacationResponseDTO> findAll(Long requesterId) {
        User requester = getRequester(requesterId);

        if (requester.getRole() == Role.ADMIN) {
            return vacationRepository.findAll().stream()
                    .map(VacationResponseDTO::fromEntity)
                    .toList();
        }

        if (requester.getRole() == Role.MANAGER) {
            return vacationRepository.findByManagerId(requester.getId()).stream()
                    .map(VacationResponseDTO::fromEntity)
                    .toList();
        }

        return vacationRepository.findByUserId(requester.getId()).stream()
                .map(VacationResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public VacationResponseDTO findById(Long id, Long requesterId) {
        User requester = getRequester(requesterId);
        VacationRequest vacation = vacationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacation request not found with id: " + id));

        validateViewPermission(requester, vacation);
        return VacationResponseDTO.fromEntity(vacation);
    }

    @Transactional
    public VacationResponseDTO create(VacationCreateRequestDTO dto, Long requesterId) {
        User requester = getRequester(requesterId);
        User targetUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        if (requester.getRole() == Role.COLLABORATOR && !requester.getId().equals(targetUser.getId())) {
            throw new UnauthorizedOperationException("Collaborators can only create their own vacation requests");
        }

        validateDates(dto.getStartDate(), dto.getEndDate());
        checkOverlap(dto.getStartDate(), dto.getEndDate(), null);

        VacationRequest request = VacationRequest.builder()
                .user(targetUser)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(VacationStatus.PENDING)
                .build();

        return VacationResponseDTO.fromEntity(vacationRepository.save(request));
    }

    @Transactional
    public VacationResponseDTO update(Long id, VacationUpdateRequestDTO dto, Long requesterId) {
        User requester = getRequester(requesterId);
        VacationRequest vacation = vacationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacation request not found with id: " + id));

        if (requester.getRole() == Role.COLLABORATOR && !vacation.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedOperationException("Collaborators can only edit their own vacation requests");
        }

        if (vacation.getStatus() != VacationStatus.PENDING) {
            throw new BusinessException("Only pending vacation requests can be edited");
        }

        validateDates(dto.getStartDate(), dto.getEndDate());
        checkOverlap(dto.getStartDate(), dto.getEndDate(), id);

        vacation.setStartDate(dto.getStartDate());
        vacation.setEndDate(dto.getEndDate());

        return VacationResponseDTO.fromEntity(vacationRepository.save(vacation));
    }

    @Transactional
    public VacationResponseDTO updateStatus(Long id, VacationStatusUpdateDTO dto, Long requesterId) {
        User requester = getRequester(requesterId);
        VacationRequest vacation = vacationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacation request not found with id: " + id));

        if (dto.getStatus() == VacationStatus.PENDING) {
            throw new BusinessException("Cannot reset status back to PENDING");
        }

        if (requester.getRole() == Role.ADMIN) {
            vacation.setStatus(dto.getStatus());
            return VacationResponseDTO.fromEntity(vacationRepository.save(vacation));
        }

        if (requester.getRole() == Role.MANAGER) {
            boolean isManagerOfUser = vacation.getUser().getManager() != null
                    && vacation.getUser().getManager().getId().equals(requester.getId());
            if (!isManagerOfUser) {
                throw new UnauthorizedOperationException("Managers can only approve/reject requests for their own collaborators");
            }
            vacation.setStatus(dto.getStatus());
            return VacationResponseDTO.fromEntity(vacationRepository.save(vacation));
        }

        throw new UnauthorizedOperationException("Collaborators cannot approve or reject vacation requests");
    }

    @Transactional
    public void cancel(Long id, Long requesterId) {
        User requester = getRequester(requesterId);
        VacationRequest vacation = vacationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacation request not found with id: " + id));

        if (requester.getRole() == Role.COLLABORATOR && !vacation.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedOperationException("Collaborators can only cancel their own vacation requests");
        }

        if (vacation.getStatus() == VacationStatus.REJECTED || vacation.getStatus() == VacationStatus.CANCELLED) {
            throw new BusinessException("Request is already finished with status: " + vacation.getStatus());
        }

        vacation.setStatus(VacationStatus.CANCELLED);
        vacationRepository.save(vacation);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessException("Start date must be before or equal to end date");
        }
    }

    private void checkOverlap(LocalDate start, LocalDate end, Long excludeRequestId) {
        List<VacationStatus> activeStatuses = List.of(VacationStatus.PENDING, VacationStatus.APPROVED);
        boolean overlaps = vacationRepository.existsOverlappingVacation(start, end, activeStatuses, excludeRequestId);
        if (overlaps) {
            throw new BusinessException("There is already a vacation request scheduled for this period across the company");
        }
    }

    private User getRequester(Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedOperationException("Requester ID header (X-User-Id) is required");
        }
        return userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester user not found with id: " + requesterId));
    }

    private void validateViewPermission(User requester, VacationRequest vacation) {
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() == Role.MANAGER) {
            boolean isManager = vacation.getUser().getManager() != null
                    && vacation.getUser().getManager().getId().equals(requester.getId());
            if (isManager || vacation.getUser().getId().equals(requester.getId())) return;
        }
        if (requester.getRole() == Role.COLLABORATOR && vacation.getUser().getId().equals(requester.getId())) return;

        throw new UnauthorizedOperationException("Access denied to this vacation request");
    }
}