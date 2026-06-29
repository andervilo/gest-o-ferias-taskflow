package com.taskflow.gestaocolaboradores.employee.infrastructure;

import com.taskflow.gestaocolaboradores.employee.application.dto.EmployeeResult;
import com.taskflow.gestaocolaboradores.employee.domain.Email;
import com.taskflow.gestaocolaboradores.employee.domain.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    // Domain → JPA Entity (MapStruct gerado; Email VO → String via email.value)
    @Mapping(target = "email", source = "email.value")
    EmployeeJpaEntity toEntity(Employee domain);

    // Domain → Result DTO (MapStruct gerado)
    @Mapping(target = "email", source = "email.value")
    EmployeeResult toResult(Employee domain);

    // JPA Entity → Domain (implementação manual — usa factory Employee.reconstitute)
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

    // Converte String → Email VO (usado por MapStruct no mapeamento inverso se necessário)
    default Email toEmail(String value) {
        return value == null ? null : new Email(value);
    }
}
