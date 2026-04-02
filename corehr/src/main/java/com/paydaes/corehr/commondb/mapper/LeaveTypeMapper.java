package com.paydaes.corehr.commondb.mapper;

import com.paydaes.entities.dto.commondb.LeaveTypeDto;
import com.paydaes.entities.model.commondb.LeaveType;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LeaveTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", expression = "java(dto.getIsActive() != null ? dto.getIsActive() : true)")
    @Mapping(target = "paid", expression = "java(dto.getIsPaid() != null ? dto.getIsPaid() : true)")
    @Mapping(target = "carryForwardDays", expression = "java(dto.getCarryForwardDays() != null ? dto.getCarryForwardDays() : 0)")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LeaveType toEntity(LeaveTypeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "paid", source = "isPaid")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveTypeDto dto, @MappingTarget LeaveType leaveType);

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "isPaid", source = "paid")
    LeaveTypeDto toDto(LeaveType leaveType);
}
