package com.taskflow.gestaocolaboradores.vacation.infrastructure;

import com.taskflow.gestaocolaboradores.vacation.domain.VacationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface VacationJpaRepository
        extends JpaRepository<VacationJpaEntity, UUID>,
                JpaSpecificationExecutor<VacationJpaEntity> {

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status = :status
            AND v.employeeId <> :excludeEmployeeId
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusAndEmployeeIdNotAndPeriodOverlap(
            @Param("status") VacationStatus status,
            @Param("excludeEmployeeId") UUID excludeEmployeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status = :status
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusAndPeriodOverlap(
            @Param("status") VacationStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status = :status
            AND v.employeeId IN :employeeIds
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusAndEmployeeIdInAndPeriodOverlap(
            @Param("status") VacationStatus status,
            @Param("employeeIds") List<UUID> employeeIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status IN :statuses
            AND v.employeeId <> :excludeEmployeeId
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusInAndEmployeeIdNotAndPeriodOverlap(
            @Param("statuses") Collection<VacationStatus> statuses,
            @Param("excludeEmployeeId") UUID excludeEmployeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status IN :statuses
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusInAndPeriodOverlap(
            @Param("statuses") Collection<VacationStatus> statuses,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT v FROM VacationJpaEntity v
            WHERE v.status IN :statuses
            AND v.employeeId IN :employeeIds
            AND v.startDate <= :to
            AND v.endDate >= :from
            """)
    List<VacationJpaEntity> findByStatusInAndEmployeeIdInAndPeriodOverlap(
            @Param("statuses") Collection<VacationStatus> statuses,
            @Param("employeeIds") Collection<UUID> employeeIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
