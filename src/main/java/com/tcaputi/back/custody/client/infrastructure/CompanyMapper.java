package com.tcaputi.back.custody.client.infrastructure;

import com.tcaputi.back.custody.client.domain.model.Company;
import com.tcaputi.back.custody.client.interfaces.dto.CompanyDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {
    Company toEntity(CompanyDto companyDto);

    CompanyDto toDto(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Company partialUpdate(CompanyDto companyDto, @MappingTarget Company company);
}