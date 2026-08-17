package com.taskflow.vacation.repository;

import com.taskflow.vacation.domain.entity.VacationRequest;
import com.taskflow.vacation.domain.enums.VacationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByUserId(Long userId);

    @Query("SELECT v FROM VacationRequest v WHERE v.user.manager.id = :managerId")
    List<VacationRequest> findByManagerId(@Param("managerId") Long managerId);

    @Query("""
        SELECT COUNT(v) > 0 FROM VacationRequest v 
        WHERE v.status IN (:activeStatuses)
        AND (:excludeRequestId IS NULL OR v.id != :excludeRequestId)
        AND v.startDate <= :endDate 
        AND v.endDate >= :startDate
    """)
    boolean existsOverlappingVacation(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("activeStatuses") List<VacationStatus> activeStatuses,
            @Param("excludeRequestId") Long excludeRequestId
    );
}