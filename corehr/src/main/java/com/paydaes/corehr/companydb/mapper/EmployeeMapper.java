package com.paydaes.corehr.companydb.mapper;

import com.paydaes.entities.dto.corehr.EmployeeDto;
import com.paydaes.entities.model.corehr.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = Employee.class)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", defaultExpression = "java(Employee.EmployeeStatus.ACTIVE)")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Employee toEntity(EmployeeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EmployeeDto dto, @MappingTarget Employee employee);

    EmployeeDto toDto(Employee employee);
}
