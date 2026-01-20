package com.tcaputi.back.custody.tax.infrastructure;

import com.tcaputi.back.custody.tax.domain.model.TaxProfile;
import com.tcaputi.back.custody.tax.interfaces.dto.TaxProfileDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaxProfileMapper {
    TaxProfile toEntity(TaxProfileDto taxProfileDto);

    TaxProfileDto toDto(TaxProfile taxProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TaxProfile partialUpdate(TaxProfileDto taxProfileDto, @MappingTarget TaxProfile taxProfile);
}