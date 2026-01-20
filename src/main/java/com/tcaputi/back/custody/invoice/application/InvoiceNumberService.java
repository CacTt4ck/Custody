package com.tcaputi.back.custody.invoice.application;

import com.tcaputi.back.custody.invoice.domain.model.InvoiceType;
import com.tcaputi.back.custody.invoice.infrastructure.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceNumberService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Génère le prochain numéro de facture légal français
     * Format: {PREFIX}-{ANNEE}-{NUMERO_SEQUENTIEL}
     * Ex: FA-2025-0001, AV-2025-0001
     */
    @Transactional
    public synchronized String generateInvoiceNumber(InvoiceType type) {
        int currentYear = LocalDate.now().getYear();
        String prefix = type.getPrefix();
        
        // Recherche du dernier numéro pour ce type et cette année
        String pattern = prefix + "-" + currentYear + "-%";
        
        Optional<String> lastNumber = invoiceRepository.findLastInvoiceNumberByPattern(pattern);

        String newNumber = this.getNewNumber(lastNumber, prefix, currentYear);

        log.info("Nouveau numéro de facture généré : {}", newNumber);
        return newNumber;
    }

    private String getNewNumber(Optional<String> lastNumber, String prefix, int currentYear) {
        int nextSequence;
        if (lastNumber.isPresent()) {
            // Extrait le numéro séquentiel du dernier numéro
            String lastNumberValue = lastNumber.get();
            String sequencePart = lastNumberValue.substring(lastNumberValue.lastIndexOf("-") + 1);
            nextSequence = Integer.parseInt(sequencePart) + 1;
        } else {
            // Premier numéro de l'année pour ce type
            nextSequence = 1;
        }

        return String.format("%s-%d-%04d", prefix, currentYear, nextSequence);
    }

    /**
     * Valide qu'un numéro de facture respecte le format légal
     */
    public boolean isValidInvoiceNumber(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }
        
        // Format attendu: PREFIX-YYYY-NNNN
        String regex = "^(FA|AV)-\\d{4}-\\d{4}$";
        return number.matches(regex);
    }
}
