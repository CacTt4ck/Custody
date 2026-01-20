package com.tcaputi.back.custody.quote.application;

import com.tcaputi.back.custody.client.infrastructure.ClientRepository;
import com.tcaputi.back.custody.project.infrastructure.ProjectRepository;
import com.tcaputi.back.custody.quote.domain.model.Quote;
import com.tcaputi.back.custody.quote.domain.model.QuoteLine;
import com.tcaputi.back.custody.quote.domain.model.QuoteStatus;
import com.tcaputi.back.custody.quote.infrastructure.QuoteMapper;
import com.tcaputi.back.custody.quote.infrastructure.QuoteRepository;
import com.tcaputi.back.custody.quote.interfaces.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final QuoteMapper quoteMapper;

    public Page<QuoteDto> getQuotes(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = quoteRepository.findAll(pageable);
        return quotes.map(quoteMapper::toDto);
    }

    public Optional<QuoteDto> getQuoteById(UUID id) {
        return quoteRepository.findById(id)
                .map(quoteMapper::toDto);
    }

    public int getQuoteCountByStatus(QuoteStatus status) {
        return quoteRepository.countByStatus(status);
    }

    public Page<QuoteDto> getQuotesByClient(UUID clientId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = quoteRepository.findByClientId(clientId, pageable);
        return quotes.map(quoteMapper::toDto);
    }

    public Page<QuoteDto> getQuotesByProject(UUID projectId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = quoteRepository.findByProjectId(projectId, pageable);
        return quotes.map(quoteMapper::toDto);
    }

    public Page<QuoteDto> getQuotesByStatus(QuoteStatus status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = quoteRepository.findByStatus(status, pageable);
        return quotes.map(quoteMapper::toDto);
    }

    public List<QuoteDto> searchQuotesByNumber(String number) {
        List<Quote> quotes = quoteRepository.findByNumberContaining(number);
        return quoteMapper.toDtoList(quotes);
    }

    @Transactional
    public QuoteDto createQuote(QuoteDto quoteDto) {
        log.debug("Création d'un nouveau devis pour le client : {}", quoteDto.clientId());

        // Vérifier que le client existe
        if (!clientRepository.existsById(quoteDto.clientId())) {
            throw new IllegalArgumentException("Client introuvable : " + quoteDto.clientId());
        }

        // Vérifier que le projet existe si spécifié
        if (quoteDto.projectId() != null && !projectRepository.existsById(quoteDto.projectId())) {
            throw new IllegalArgumentException("Projet introuvable : " + quoteDto.projectId());
        }

        Quote quote = quoteMapper.toEntity(quoteDto);
        
        // Générer un numéro si pas fourni
        if (quote.getNumber() == null || quote.getNumber().trim().isEmpty()) {
            quote.setNumber(generateQuoteNumber());
        }

        // Statut par défaut
        if (quote.getStatus() == null) {
            quote.setStatus(QuoteStatus.DRAFT);
        }

        // Date d'émission par défaut
        if (quote.getIssueDate() == null) {
            quote.setIssueDate(LocalDate.now());
        }

        // Traiter les lignes
        if (quoteDto.lines() != null && !quoteDto.lines().isEmpty()) {
            List<QuoteLine> lines = quoteMapper.toLineEntityList(quoteDto.lines());
            lines.forEach(line -> line.setQuote(quote));
            quote.setLines(lines);
        }

        // Calculer les totaux
        calculateTotals(quote);

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Devis créé avec l'ID : {}", savedQuote.getId());

        return quoteMapper.toDto(savedQuote);
    }

    @Transactional
    public Optional<QuoteDto> updateQuote(UUID id, QuoteDto quoteDto) {
        if (!id.equals(quoteDto.id())) {
            throw new IllegalArgumentException("L'ID dans l'URL ne correspond pas à l'ID dans le body");
        }

        Optional<Quote> existingQuoteOpt = quoteRepository.findById(id);
        if (existingQuoteOpt.isEmpty()) {
            log.warn("Tentative de mise à jour d'un devis inexistant : {}", id);
            return Optional.empty();
        }

        Quote existingQuote = existingQuoteOpt.get();
        
        // Vérifier que le client existe
        if (!clientRepository.existsById(quoteDto.clientId())) {
            throw new IllegalArgumentException("Client introuvable : " + quoteDto.clientId());
        }

        // Vérifier que le projet existe si spécifié
        if (quoteDto.projectId() != null && !projectRepository.existsById(quoteDto.projectId())) {
            throw new IllegalArgumentException("Projet introuvable : " + quoteDto.projectId());
        }

        log.debug("Mise à jour du devis : {}", id);

        // Mettre à jour les propriétés de base
        quoteMapper.updateEntityFromDto(quoteDto, existingQuote);
        
        // Mettre à jour les relations
        existingQuote.setClient(clientRepository.getReferenceById(quoteDto.clientId()));
        if (quoteDto.projectId() != null) {
            existingQuote.setProject(projectRepository.getReferenceById(quoteDto.projectId()));
        } else {
            existingQuote.setProject(null);
        }

        // Mettre à jour les lignes
        existingQuote.getLines().clear();
        if (quoteDto.lines() != null && !quoteDto.lines().isEmpty()) {
            List<QuoteLine> lines = quoteMapper.toLineEntityList(quoteDto.lines());
            lines.forEach(line -> line.setQuote(existingQuote));
            existingQuote.getLines().addAll(lines);
        }

        // Recalculer les totaux
        calculateTotals(existingQuote);

        Quote updatedQuote = quoteRepository.save(existingQuote);
        return Optional.of(quoteMapper.toDto(updatedQuote));
    }

    @Transactional
    public boolean deleteQuote(UUID id) {
        if (!quoteRepository.existsById(id)) {
            log.warn("Tentative de suppression d'un devis inexistant : {}", id);
            return false;
        }

        log.debug("Suppression du devis : {}", id);
        quoteRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<QuoteDto> updateQuoteStatus(UUID id, QuoteStatus newStatus) {
        Optional<Quote> quoteOpt = quoteRepository.findById(id);
        if (quoteOpt.isEmpty()) {
            return Optional.empty();
        }

        Quote quote = quoteOpt.get();
        
        // Vérifier si le changement de statut est valide
        if (!isValidStatusTransition(quote.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                String.format("Transition de statut invalide : %s -> %s", quote.getStatus(), newStatus)
            );
        }

        quote.setStatus(newStatus);
        Quote updatedQuote = quoteRepository.save(quote);
        
        log.info("Statut du devis {} mis à jour : {}", id, newStatus);
        return Optional.of(quoteMapper.toDto(updatedQuote));
    }

    public List<QuoteDto> getExpiredQuotes() {
        List<Quote> expiredQuotes = quoteRepository.findByValidUntilBefore(LocalDate.now());
        return quoteMapper.toDtoList(expiredQuotes);
    }

    private void calculateTotals(Quote quote) {
        if (quote.getLines() == null || quote.getLines().isEmpty()) {
            quote.setSubtotal(BigDecimal.ZERO);
            quote.setTaxTotal(BigDecimal.ZERO);
            quote.setTotal(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (QuoteLine line : quote.getLines()) {
            BigDecimal quantity = line.getQuantity() != null ? line.getQuantity() : BigDecimal.ONE;
            BigDecimal unitPrice = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;
            BigDecimal taxRate = line.getTaxRate() != null ? line.getTaxRate() : BigDecimal.ZERO;

            // Calcul du montant HT après remise
            BigDecimal lineSubtotal = quantity.multiply(unitPrice).subtract(discount);
            subtotal = subtotal.add(lineSubtotal);

            // Calcul de la TVA
            BigDecimal lineTax = lineSubtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
            taxTotal = taxTotal.add(lineTax);
        }

        quote.setSubtotal(subtotal);
        quote.setTaxTotal(taxTotal);
        quote.setTotal(subtotal.add(taxTotal));
    }

    private boolean isValidStatusTransition(QuoteStatus currentStatus, QuoteStatus newStatus) {
        return switch (currentStatus) {
            case DRAFT -> newStatus == QuoteStatus.SENT;
            case SENT -> newStatus == QuoteStatus.ACCEPTED || 
                        newStatus == QuoteStatus.REJECTED || 
                        newStatus == QuoteStatus.EXPIRED;
            case ACCEPTED, REJECTED, EXPIRED -> false; // États finaux
        };
    }

    private String generateQuoteNumber() {
        // Logique de génération du numéro de devis
        int year = LocalDate.now().getYear();
        long count = quoteRepository.count() + 1;
        return String.format("Q-%d-%03d", year, count);
    }
}
