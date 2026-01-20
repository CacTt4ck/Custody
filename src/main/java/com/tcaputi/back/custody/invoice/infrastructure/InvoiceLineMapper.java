package com.tcaputi.back.custody.invoice.infrastructure;

import com.tcaputi.back.custody.invoice.domain.model.InvoiceLine;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceLineDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InvoiceLineMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    InvoiceLineDto toDto(InvoiceLine line);

    @Mapping(target = "invoice", ignore = true) // Assigné manuellement dans le service
    InvoiceLine toEntity(InvoiceLineDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "invoice", ignore = true)
    InvoiceLine partialUpdate(InvoiceLineDto dto, @MappingTarget InvoiceLine line);
}
