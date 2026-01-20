package com.tcaputi.back.custody.invoice.application;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.client.infrastructure.ClientRepository;
import com.tcaputi.back.custody.invoice.domain.model.Invoice;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceLine;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceStatus;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceType;
import com.tcaputi.back.custody.invoice.infrastructure.InvoiceLineMapper;
import com.tcaputi.back.custody.invoice.infrastructure.InvoiceMapper;
import com.tcaputi.back.custody.invoice.infrastructure.InvoiceRepository;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceDto;
import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.project.infrastructure.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineMapper invoiceLineMapper;
    private final InvoiceNumberService invoiceNumberService;

    public Page<InvoiceDto> getInvoices(Integer page, Integer size) {
        Page<Invoice> invoices = invoiceRepository.findAll(PageRequest.of(page, size));
        return invoices.map(invoiceMapper::toDto);
    }

    public Optional<InvoiceDto> getInvoiceById(UUID id) {
        return invoiceRepository.findById(id)
                .map(invoiceMapper::toDto);
    }

    public int getOverdueInvoiceCount() {
        return invoiceRepository.countOverdueInvoices(LocalDate.now());
    }

    public List<InvoiceDto> getInvoicesByClient(UUID clientId) {
        log.debug("Récupération des factures pour le client : {}", clientId);
        List<Invoice> invoices = invoiceRepository.findByClientId(clientId);
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    public List<InvoiceDto> getInvoicesByProject(UUID projectId) {
        log.debug("Récupération des factures pour le projet : {}", projectId);
        List<Invoice> invoices = invoiceRepository.findByProjectId(projectId);
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    public List<InvoiceDto> getInvoicesByStatus(InvoiceStatus status) {
        log.debug("Récupération des factures avec le statut : {}", status);
        List<Invoice> invoices = invoiceRepository.findByStatus(status);
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    public List<InvoiceDto> searchInvoices(UUID clientId, UUID projectId, InvoiceStatus status, String number) {
        log.debug("Recherche de factures avec les critères : clientId={}, projectId={}, status={}, number={}", 
                clientId, projectId, status, number);

        String cleanNumber = cleanParameter(number);

        List<Invoice> invoices = invoiceRepository.findInvoicesByMultipleCriteria(
                clientId, projectId, status, cleanNumber
        );

        return invoices.stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    public List<InvoiceDto> getOverdueInvoices() {
        log.debug("Récupération des factures en retard");
        List<Invoice> invoices = invoiceRepository.findOverdueInvoices(LocalDate.now());
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto dto) {
        log.debug("Création d'une nouvelle facture : {}", dto.number());

        // Validation du client
        if (!clientRepository.existsById(dto.clientId())) {
            throw new IllegalArgumentException("Le client avec l'ID " + dto.clientId() + " n'existe pas");
        }

        // Validation du projet (optionnel)
        if (dto.projectId() != null && !projectRepository.existsById(dto.projectId())) {
            throw new IllegalArgumentException("Le projet avec l'ID " + dto.projectId() + " n'existe pas");
        }

        Invoice entity = invoiceMapper.toEntity(dto);

        // Définir le type de facture (par défaut FACTURE)
        if (entity.getType() == null) {
            entity.setType(InvoiceType.FACTURE);
        }

        // Génération automatique du numéro AVANT la validation d'unicité
        if (entity.getNumber() == null || entity.getNumber().trim().isEmpty()) {
            entity.setNumber(invoiceNumberService.generateInvoiceNumber(entity.getType()));
        } else {
            // Validation du format si un numéro est fourni
            if (!invoiceNumberService.isValidInvoiceNumber(entity.getNumber())) {
                throw new IllegalArgumentException("Le format du numéro de facture ne respecte pas la réglementation française");
            }
            // Validation de l'unicité du numéro
            if (invoiceRepository.existsByNumber(entity.getNumber())) {
                throw new IllegalArgumentException("Une facture avec le numéro " + entity.getNumber() + " existe déjà");
            }
        }

        // Validation des dates
        validateInvoiceDates(dto);

        // Assignation du client
        Client client = clientRepository.findById(dto.clientId()).orElseThrow();
        entity.setClient(client);

        // Assignation du projet si présent
        if (dto.projectId() != null) {
            Project project = projectRepository.findById(dto.projectId()).orElseThrow();
            entity.setProject(project);
        }

        // Gestion des lignes de facture
        if (dto.lines() != null && !dto.lines().isEmpty()) {
            List<InvoiceLine> lines = dto.lines().stream()
                    .map(invoiceLineMapper::toEntity)
                    .toList();

            lines.forEach(line -> line.setInvoice(entity));
            entity.setLines(lines);
        }

        // Calcul automatique des totaux
        calculateTotals(entity);

        Invoice savedInvoice = invoiceRepository.save(entity);

        log.info("Facture créée avec l'ID : {} et le numéro : {}", savedInvoice.getId(), savedInvoice.getNumber());
        return invoiceMapper.toDto(savedInvoice);
    }


    @Transactional
    public boolean deleteInvoice(UUID id) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (invoiceOpt.isEmpty()) {
            log.warn("Tentative de suppression d'une facture inexistante : {}", id);
            return false;
        }

        Invoice invoice = invoiceOpt.get();
        
        // Vérification du statut avant suppression
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Seules les factures au statut DRAFT peuvent être supprimées");
        }
        
        log.debug("Suppression de la facture : {}", id);
        invoiceRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<InvoiceDto> updateInvoice(UUID id, InvoiceDto dto) {
        // Validation de cohérence ID
        if (!id.equals(dto.id())) {
            throw new IllegalArgumentException("L'ID dans l'URL ne correspond pas à l'ID dans le body");
        }

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findById(id);
        if (existingInvoiceOpt.isEmpty()) {
            log.warn("Tentative de mise à jour d'une facture inexistante : {}", id);
            return Optional.empty();
        }

        Invoice existingInvoice = existingInvoiceOpt.get();

        // Vérification du statut pour les modifications
        if (existingInvoice.getStatus() == InvoiceStatus.PAID || existingInvoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Les factures payées ou annulées ne peuvent pas être modifiées");
        }

        // Validation du client
        if (!clientRepository.existsById(dto.clientId())) {
            throw new IllegalArgumentException("Le client avec l'ID " + dto.clientId() + " n'existe pas");
        }

        // Validation du projet (optionnel)
        if (dto.projectId() != null && !projectRepository.existsById(dto.projectId())) {
            throw new IllegalArgumentException("Le projet avec l'ID " + dto.projectId() + " n'existe pas");
        }

        // Validation de l'unicité du numéro (sauf pour la facture actuelle)
        if (dto.number() != null && invoiceRepository.existsByNumberAndIdNot(dto.number(), id)) {
            throw new IllegalArgumentException("Une autre facture avec le numéro " + dto.number() + " existe déjà");
        }

        // Validation des dates
        validateInvoiceDates(dto);

        log.debug("Mise à jour de la facture : {}", id);
        
        Invoice entity = invoiceMapper.toEntity(dto);
        entity.setId(id); // S'assurer que l'ID est préservé
        
        // Assignation du client
        Client client = clientRepository.findById(dto.clientId()).orElseThrow();
        entity.setClient(client);

        // Assignation du projet si présent
        if (dto.projectId() != null) {
            Project project = projectRepository.findById(dto.projectId()).orElseThrow();
            entity.setProject(project);
        }

        // Gestion des lignes de facture
        entity.getLines().clear(); // Suppression des anciennes lignes
        if (dto.lines() != null && !dto.lines().isEmpty()) {
            List<InvoiceLine> lines = dto.lines().stream()
                    .map(invoiceLineMapper::toEntity)
                    .toList();
            
            lines.forEach(line -> line.setInvoice(entity));
            entity.getLines().addAll(lines);
        }

        // Recalcul des totaux
        calculateTotals(entity);

        Invoice updatedInvoice = invoiceRepository.save(entity);
        
        return Optional.of(invoiceMapper.toDto(updatedInvoice));
    }

    @Transactional
    public Optional<InvoiceDto> changeInvoiceStatus(UUID id, InvoiceStatus newStatus) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();
        validateStatusTransition(invoice.getStatus(), newStatus);
        
        invoice.setStatus(newStatus);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        log.info("Statut de la facture {} changé de {} à {}", invoice.getNumber(), invoice.getStatus(), newStatus);
        return Optional.of(invoiceMapper.toDto(updatedInvoice));
    }

    // Méthodes privées utilitaires

    private void validateInvoiceDates(InvoiceDto dto) {
        LocalDate now = LocalDate.now();
        
        if (dto.issueDate() != null && dto.dueDate() != null) {
            if (dto.dueDate().isBefore(dto.issueDate())) {
                throw new IllegalArgumentException("La date d'échéance ne peut pas être antérieure à la date d'émission");
            }
        }

        if (dto.supplyDate() != null && dto.issueDate() != null) {
            if (dto.supplyDate().isAfter(dto.issueDate())) {
                throw new IllegalArgumentException("La date de prestation ne peut pas être postérieure à la date d'émission");
            }
        }
    }

    private void validateStatusTransition(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        // Implémentation des transitions de statut autorisées
        switch (currentStatus) {
            case DRAFT:
                if (newStatus != InvoiceStatus.SENT && newStatus != InvoiceStatus.CANCELLED) {
                    throw new IllegalStateException("Transition de statut non autorisée de " + currentStatus + " vers " + newStatus);
                }
                break;
            case SENT:
                if (newStatus != InvoiceStatus.PARTIALLY_PAID && newStatus != InvoiceStatus.PAID && 
                    newStatus != InvoiceStatus.OVERDUE && newStatus != InvoiceStatus.CANCELLED) {
                    throw new IllegalStateException("Transition de statut non autorisée de " + currentStatus + " vers " + newStatus);
                }
                break;
            case PARTIALLY_PAID:
                if (newStatus != InvoiceStatus.PAID && newStatus != InvoiceStatus.OVERDUE) {
                    throw new IllegalStateException("Transition de statut non autorisée de " + currentStatus + " vers " + newStatus);
                }
                break;
            case PAID:
            case CANCELLED:
                throw new IllegalStateException("Les factures " + currentStatus + " ne peuvent pas changer de statut");
        }
    }

    private void calculateTotals(Invoice invoice) {
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            invoice.setSubtotal(BigDecimal.ZERO);
            invoice.setTaxTotal(BigDecimal.ZERO);
            invoice.setTotal(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (InvoiceLine line : invoice.getLines()) {
            BigDecimal lineAmount = calculateLineAmount(line);
            BigDecimal lineDiscount = calculateLineDiscount(line, lineAmount);
            BigDecimal lineNetAmount = lineAmount.subtract(lineDiscount);
            BigDecimal lineTax = calculateLineTax(line, lineNetAmount);

            subtotal = subtotal.add(lineNetAmount);
            taxTotal = taxTotal.add(lineTax);
        }

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxTotal(taxTotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotal(subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateLineAmount(InvoiceLine line) {
        BigDecimal quantity = line.getQuantity() != null ? line.getQuantity() : BigDecimal.ONE;
        BigDecimal unitPrice = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
        return quantity.multiply(unitPrice);
    }

    private BigDecimal calculateLineDiscount(InvoiceLine line, BigDecimal lineAmount) {
        BigDecimal discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;
        return lineAmount.multiply(discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLineTax(InvoiceLine line, BigDecimal lineNetAmount) {
        BigDecimal taxRate = line.getTaxRate() != null ? line.getTaxRate() : BigDecimal.ZERO;
        return lineNetAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String generateInvoiceNumber() {
        LocalDate now = LocalDate.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // Recherche du dernier numéro pour ce mois
        String pattern = "INV-" + datePrefix + "-%";
        
        // Cette requête nécessiterait une méthode dans le repository
        // Pour simplifier, on génère un numéro basique
        return "INV-" + datePrefix + "-" + String.format("%03d", System.currentTimeMillis() % 1000);
    }

    private String cleanParameter(String parameter) {
        return (parameter != null && !parameter.trim().isEmpty()) ? parameter.trim() : null;
    }
}
