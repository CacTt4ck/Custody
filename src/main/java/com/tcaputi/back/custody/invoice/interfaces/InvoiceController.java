package com.tcaputi.back.custody.invoice.interfaces;

import com.tcaputi.back.custody.common.dto.EmailRequestDto;
import com.tcaputi.back.custody.invoice.application.InvoicePdfService;
import com.tcaputi.back.custody.invoice.application.InvoiceService;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceStatus;
import com.tcaputi.back.custody.invoice.infrastructure.EmailService;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
class InvoiceController {

    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getInvoices(
            @RequestParam(defaultValue = "0") Integer page, 
            @RequestParam(defaultValue = "20") Integer size) {
        Page<InvoiceDto> invoices = invoiceService.getInvoices(page, size);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/count/overdue")
    public ResponseEntity<Integer> getOverdueInvoiceCount() {
        int count = invoiceService.getOverdueInvoiceCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable UUID id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByClient(@PathVariable UUID clientId) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByClient(clientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByProject(@PathVariable UUID projectId) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByProject(projectId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        List<InvoiceDto> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvoiceDto>> searchInvoices(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String number) {
        
        List<InvoiceDto> invoices = invoiceService.searchInvoices(clientId, projectId, status, number);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody InvoiceDto dto) {
        try {
            InvoiceDto createdInvoice = invoiceService.createInvoice(dto);
            return ResponseEntity.created(URI.create("/invoices/" + createdInvoice.id()))
                    .body(createdInvoice);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Erreur lors de la création de la facture : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        try {
            boolean deleted = invoiceService.deleteInvoice(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Erreur lors de la suppression de la facture : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable UUID id, @RequestBody InvoiceDto dto) {
        try {
            return invoiceService.updateInvoice(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Erreur lors de la mise à jour de la facture : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDto> changeInvoiceStatus(
            @PathVariable UUID id, 
            @RequestParam InvoiceStatus status) {
        try {
            return invoiceService.changeInvoiceStatus(id, status)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            log.warn("Erreur lors du changement de statut : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable UUID id) {
        try {
            return invoiceService.getInvoiceById(id)
                    .map(invoice -> {
                        byte[] pdfContent = invoicePdfService.generateInvoicePdf(invoice);
                        String filename = "facture-" + invoice.number() + ".pdf";
                        
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdfContent);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la facture {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/email")
    public ResponseEntity<String> sendInvoiceEmail(@PathVariable UUID id, @Valid @RequestBody EmailRequestDto emailRequest) {
        return invoiceService.getInvoiceById(id)
                .map(invoice -> {
                    byte[] pdfContent = invoicePdfService.generateInvoicePdf(invoice);
                    emailService.sendInvoiceByEmail(emailRequest.email(), emailRequest.subject(), emailRequest.content(), pdfContent, invoice.number());
                    return ResponseEntity.ok("Email envoyé avec succès");
                }).orElse(ResponseEntity.notFound().build());
    }

}
