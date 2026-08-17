package com.taskflow.vacation.repository;

import com.taskflow.vacation.domain.entity.VacationRequest;
import com.taskflow.vacation.domain.enums.VacationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRepository extends JpaRepository<VacationRequest, Long> {

    @Query("SELECT v FROM VacationRequest v WHERE " +
            "(:userId IS NULL OR v.user.id = :userId) AND " +
            "(:managerId IS NULL OR v.user.manager.id = :managerId) AND " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:startDate IS NULL OR v.endDate >= :startDate) AND " +
            "(:endDate IS NULL OR v.startDate <= :endDate)")
    Page<VacationRequest> findWithFilters(
            @Param("userId") Long userId,
            @Param("managerId") Long managerId,
            @Param("status") VacationStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT v FROM VacationRequest v WHERE " +
            "v.status NOT IN ('CANCELLED', 'REJECTED') AND " +
            "v.startDate <= :endDate AND v.endDate >= :startDate")
    List<VacationRequest> findOverlappingVacations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}