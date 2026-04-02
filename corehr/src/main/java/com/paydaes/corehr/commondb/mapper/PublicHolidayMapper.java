package com.paydaes.corehr.commondb.mapper;

import com.paydaes.entities.dto.commondb.PublicHolidayDto;
import com.paydaes.entities.model.commondb.PublicHoliday;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PublicHolidayMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "year", expression = "java(dto.getHolidayDate() != null ? dto.getHolidayDate().getYear() : 0)")
    @Mapping(target = "active", expression = "java(dto.getIsActive() != null ? dto.getIsActive() : true)")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PublicHoliday toEntity(PublicHolidayDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "year", expression = "java(dto.getHolidayDate() != null ? dto.getHolidayDate().getYear() : holiday.getYear())")
    @Mapping(target = "active", source = "isActive")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PublicHolidayDto dto, @MappingTarget PublicHoliday holiday);

    @Mapping(target = "isActive", source = "active")
    PublicHolidayDto toDto(PublicHoliday holiday);
}
