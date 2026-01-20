package com.tcaputi.back.custody.invoice.infrastructure;

import com.tcaputi.back.custody.invoice.domain.model.Invoice;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {InvoiceLineMapper.class})
public interface InvoiceMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "project.id", target = "projectId")
    InvoiceDto toDto(Invoice invoice);

    @Mapping(target = "client", ignore = true) // Assigné manuellement dans le service
    @Mapping(target = "project", ignore = true) // Assigné manuellement dans le service
    @Mapping(target = "lines", ignore = true) // Géré séparément pour la relation bidirectionnelle
    Invoice toEntity(InvoiceDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "lines", ignore = true)
    Invoice partialUpdate(InvoiceDto dto, @MappingTarget Invoice invoice);
}

