package com.tcaputi.back.custody.quote.interfaces;

import com.tcaputi.back.custody.quote.application.QuoteService;
import com.tcaputi.back.custody.quote.domain.model.QuoteStatus;
import com.tcaputi.back.custody.quote.interfaces.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    public ResponseEntity<Page<QuoteDto>> getQuotes(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Récupération des devis - page: {}, size: {}", page, size);
        Page<QuoteDto> quotes = quoteService.getQuotes(page, size);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteDto> getQuoteById(@PathVariable UUID id) {
        log.debug("Récupération du devis avec l'ID : {}", id);
        
        Optional<QuoteDto> quote = quoteService.getQuoteById(id);
        return quote.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count/status/{status}")
    public int getQuoteCountByStatus(@PathVariable QuoteStatus status) {
        return quoteService.getQuoteCountByStatus(status);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<QuoteDto>> getQuotesByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Récupération des devis du client {} - page: {}, size: {}", clientId, page, size);
        Page<QuoteDto> quotes = quoteService.getQuotesByClient(clientId, page, size);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<QuoteDto>> getQuotesByProject(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Récupération des devis du projet {} - page: {}, size: {}", projectId, page, size);
        Page<QuoteDto> quotes = quoteService.getQuotesByProject(projectId, page, size);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<QuoteDto>> getQuotesByStatus(
            @PathVariable QuoteStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Récupération des devis avec le statut {} - page: {}, size: {}", status, page, size);
        Page<QuoteDto> quotes = quoteService.getQuotesByStatus(status, page, size);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuoteDto>> searchQuotesByNumber(@RequestParam String number) {
        log.debug("Recherche de devis par numéro : {}", number);
        List<QuoteDto> quotes = quoteService.searchQuotesByNumber(number);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<QuoteDto>> getExpiredQuotes() {
        log.debug("Récupération des devis expirés");
        List<QuoteDto> expiredQuotes = quoteService.getExpiredQuotes();
        return ResponseEntity.ok(expiredQuotes);
    }

    @PostMapping
    public ResponseEntity<QuoteDto> createQuote(@RequestBody QuoteDto quoteDto) {
        log.debug("Création d'un nouveau devis : {}", quoteDto.number());
        
        try {
            QuoteDto createdQuote = quoteService.createQuote(quoteDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuote);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la création du devis : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteDto> updateQuote(@PathVariable UUID id, @RequestBody QuoteDto quoteDto) {
        log.debug("Mise à jour du devis : {}", id);
        
        try {
            Optional<QuoteDto> updatedQuote = quoteService.updateQuote(id, quoteDto);
            return updatedQuote.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la mise à jour du devis : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<QuoteDto> updateQuoteStatus(
            @PathVariable UUID id, 
            @RequestBody QuoteStatusUpdateRequest request) {
        
        log.debug("Mise à jour du statut du devis {} vers : {}", id, request.status());
        
        try {
            Optional<QuoteDto> updatedQuote = quoteService.updateQuoteStatus(id, request.status());
            return updatedQuote.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la mise à jour du statut : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable UUID id) {
        log.debug("Suppression du devis : {}", id);
        
        boolean deleted = quoteService.deleteQuote(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }

    // Record pour la mise à jour du statut
    public record QuoteStatusUpdateRequest(QuoteStatus status) {}
}
