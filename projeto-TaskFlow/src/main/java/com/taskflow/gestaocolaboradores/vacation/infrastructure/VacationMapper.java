package com.taskflow.gestaocolaboradores.vacation.infrastructure;

import com.taskflow.gestaocolaboradores.vacation.application.dto.VacationResult;
import com.taskflow.gestaocolaboradores.vacation.domain.VacationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VacationMapper {

    @Mapping(target = "startDate", source = "period.startDate")
    @Mapping(target = "endDate", source = "period.endDate")
    VacationJpaEntity toEntity(VacationRequest domain);

    @Mapping(target = "startDate", source = "period.startDate")
    @Mapping(target = "endDate", source = "period.endDate")
    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "managerName", ignore = true)
    VacationResult toResult(VacationRequest domain);

    default VacationRequest toDomain(VacationJpaEntity entity) {
        return VacationRequest.reconstitute(
                entity.getId(), entity.getEmployeeId(),
                entity.getStartDate(), entity.getEndDate(),
                entity.getStatus(), entity.getReason(),
                entity.getDecidedBy(), entity.getDecidedAt(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
