package com.taskflow.gestaocolaboradores.employee.infrastructure;

import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.domain.Email;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "email", source = "email.value")
    EmployeeJpaEntity toEntity(Employee domain);

    @Mapping(target = "email", source = "email.value")
    EmployeeResult toResult(Employee domain);

    default Employee toDomain(EmployeeJpaEntity entity) {
        return Employee.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getManagerId(),
                entity.getPasswordHash(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    default Email toEmail(String value) {
        return value == null ? null : new Email(value);
    }
}