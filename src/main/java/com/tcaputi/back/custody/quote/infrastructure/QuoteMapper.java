package com.tcaputi.back.custody.quote.infrastructure;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.quote.domain.model.Quote;
import com.tcaputi.back.custody.quote.domain.model.QuoteLine;
import com.tcaputi.back.custody.quote.interfaces.dto.QuoteDto;
import com.tcaputi.back.custody.quote.interfaces.dto.QuoteLineDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuoteMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "projectId", source = "project.id")
    QuoteDto toDto(Quote quote);

    @Mapping(target = "client", source = "clientId")
    @Mapping(target = "project", source = "projectId")
    @Mapping(target = "lines", ignore = true)
    Quote toEntity(QuoteDto quoteDto);

    List<QuoteDto> toDtoList(List<Quote> quotes);
    
    List<Quote> toEntityList(List<QuoteDto> quoteDtos);

    QuoteLineDto toLineDto(QuoteLine quoteLine);
    
    @Mapping(target = "quote", ignore = true)
    QuoteLine toLineEntity(QuoteLineDto quoteLineDto);
    
    List<QuoteLineDto> toLineDtoList(List<QuoteLine> quoteLines);
    
    List<QuoteLine> toLineEntityList(List<QuoteLineDto> quoteLineDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "lines", ignore = true)
    void updateEntityFromDto(QuoteDto dto, @MappingTarget Quote entity);

    // Méthodes de mapping pour les relations
    default Client mapClientId(UUID clientId) {
        if (clientId == null) return null;
        Client client = new Client();
        client.setId(clientId);
        return client;
    }

    default Project mapProjectId(UUID projectId) {
        if (projectId == null) return null;
        Project project = new Project();
        project.setId(projectId);
        return project;
    }
}
