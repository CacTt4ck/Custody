package com.tcaputi.back.custody.tax.application;

import com.tcaputi.back.custody.tax.domain.model.TaxType;
import com.tcaputi.back.custody.tax.infrastructure.TaxProfileMapper;
import com.tcaputi.back.custody.tax.infrastructure.TaxProfileRepository;
import com.tcaputi.back.custody.tax.interfaces.dto.TaxProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxProfileService {

    private final TaxProfileMapper taxProfileMapper;
    private final TaxProfileRepository taxProfileRepository;
    
    public Optional<TaxProfileDto> getTaxProfileById(UUID id) {
        return taxProfileRepository.findById(id)
                .map(taxProfileMapper::toDto);
    }
    
    public Optional<TaxProfileDto> getDefaultTaxProfileForFrance() {
        return taxProfileRepository.findFirstByType(TaxType.FR_TVA_STANDARD)
                .map(taxProfileMapper::toDto);
    }

    public Optional<TaxProfileDto> getDefaultTaxProfileOuterUE() {
        return taxProfileRepository.findFirstByType(TaxType.NON_EU_EXPORT_259_1)
                .map(taxProfileMapper::toDto);
    }

    public List<TaxProfileDto> getAllTaxProfiles() {
        return taxProfileRepository.findAll()
                .stream()
                .map(taxProfileMapper::toDto)
                .toList();
    }
    
    public List<TaxProfileDto> getTaxProfilesByType(TaxType taxType) {
        return taxProfileRepository.findAllByType(taxType)
                .stream()
                .map(taxProfileMapper::toDto)
                .toList();
    }
}
