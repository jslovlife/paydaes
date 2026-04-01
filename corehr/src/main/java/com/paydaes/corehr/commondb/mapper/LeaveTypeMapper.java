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
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LeaveType toEntity(LeaveTypeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveTypeDto dto, @MappingTarget LeaveType leaveType);

    LeaveTypeDto toDto(LeaveType leaveType);
}
