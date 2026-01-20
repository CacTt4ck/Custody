package com.tcaputi.back.custody.invoice.application;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tcaputi.back.custody.client.application.ClientService;
import com.tcaputi.back.custody.client.interfaces.dto.ClientDto;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceDto;
import com.tcaputi.back.custody.invoice.interfaces.dto.InvoiceLineDto;
import com.tcaputi.back.custody.project.application.ProjectService;
import com.tcaputi.back.custody.project.interfaces.dto.ProjectDto;
import com.tcaputi.back.custody.tax.application.TaxProfileService;
import com.tcaputi.back.custody.tax.interfaces.dto.TaxProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ClientService clientService;
    private final ProjectService projectService;
    private final TaxProfileService taxProfileService;

    public byte[] generateInvoicePdf(InvoiceDto invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Police par défaut
            PdfFont font = PdfFontFactory.createFont();
            PdfFont boldFont = PdfFontFactory.createFont();

            // En-tête de la facture
            addInvoiceHeader(document, invoice, boldFont);
            
            // Informations client
            addClientInfo(document, invoice, font, boldFont);
            
            // Tableau des lignes de facture
            addInvoiceLines(document, invoice, font, boldFont);
            
            // Totaux
            addInvoiceTotals(document, invoice, font, boldFont);
            
            // Notes et mentions légales
            addNotesAndLegalMentions(document, invoice, font);

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la facture {}: {}", invoice.number(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void addInvoiceHeader(Document document, InvoiceDto invoice, PdfFont boldFont) {
        // Titre de la facture
        Paragraph title = new Paragraph("FACTURE")
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Informations de la facture
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        // Colonne gauche - Informations société
        Cell companyCell = new Cell()
                .add(new Paragraph("VOTRE ENTREPRISE").setFont(boldFont))
                .add(new Paragraph("123 Rue de la République"))
                .add(new Paragraph("75001 Paris"))
                .add(new Paragraph("France"))
                .add(new Paragraph("SIRET: 12345678901234"))
                .setBorder(null);

        // Colonne droite - Détails facture
        Cell invoiceInfoCell = new Cell()
                .add(new Paragraph("Numéro: " + invoice.number()).setFont(boldFont))
                .add(new Paragraph("Date d'émission: " + 
                    (invoice.issueDate() != null ? invoice.issueDate().format(DATE_FORMATTER) : "N/A")))
                .add(new Paragraph("Date d'échéance: " + 
                    (invoice.dueDate() != null ? invoice.dueDate().format(DATE_FORMATTER) : "N/A")))
                .add(new Paragraph("Statut: " + invoice.status()))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT);

        headerTable.addCell(companyCell);
        headerTable.addCell(invoiceInfoCell);
        
        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private void addClientInfo(Document document, InvoiceDto invoice, PdfFont font, PdfFont boldFont) {
        // Client - cette info devrait venir du ClientService mais pour simplifier...
        ClientDto client = clientService.getClientById(invoice.clientId()).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        ProjectDto project = projectService.getProjectById(invoice.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Paragraph clientTitle = new Paragraph("FACTURÉ À:")
                .setFont(boldFont)
                .setFontSize(12);
        document.add(clientTitle);

        Paragraph clientInfo = new Paragraph("Client: " + client.contactName())
                .setFont(font);
        document.add(clientInfo);

        // Ajout de l'adresse du client
        if (client.billingAddress() != null) {
            StringBuilder addressBuilder = new StringBuilder();
            if (client.billingAddress().street() != null) {
                addressBuilder.append(client.billingAddress().street());
            }
            if (client.billingAddress().zip() != null || client.billingAddress().city() != null) {
                if (!addressBuilder.isEmpty()) {
                    addressBuilder.append("\n");
                }
                if (client.billingAddress().zip() != null) {
                    addressBuilder.append(client.billingAddress().zip()).append(" ");
                }
                if (client.billingAddress().city() != null) {
                    addressBuilder.append(client.billingAddress().city());
                }
            }
            if (client.billingAddress().country() != null) {
                if (!addressBuilder.isEmpty()) {
                    addressBuilder.append("\n");
                }
                addressBuilder.append(client.billingAddress().country());
            }

            if (!addressBuilder.isEmpty()) {
                Paragraph clientAddress = new Paragraph(addressBuilder.toString())
                        .setFont(font)
                        .setMarginBottom(10);
                document.add(clientAddress);
            }
        }


        if (invoice.projectId() != null) {
            Paragraph projectInfo = new Paragraph("Projet: " + project.name())
                    .setFont(font)
                    .setMarginBottom(10);
            document.add(projectInfo);
        }

        document.add(new Paragraph("\n"));
    }

    private void addInvoiceLines(Document document, InvoiceDto invoice, PdfFont font, PdfFont boldFont) {
        if (invoice.lines() == null || invoice.lines().isEmpty()) {
            return;
        }

        // Tableau des lignes
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 10, 10, 15, 10, 15}))
                .setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        table.addHeaderCell(new Cell().add(new Paragraph("Désignation").setFont(boldFont)));
        table.addHeaderCell(new Cell().add(new Paragraph("Qté").setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Unité").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Prix unit.").setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("TVA %").setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Total HT").setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));

        // Lignes de facture
        for (InvoiceLineDto line : invoice.lines()) {
            BigDecimal quantity = line.quantity() != null ? line.quantity() : BigDecimal.ONE;
            BigDecimal unitPrice = line.unitPrice() != null ? line.unitPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = quantity.multiply(unitPrice);

            table.addCell(new Cell().add(new Paragraph(line.designation() != null ? line.designation() : "")).setFont(font));
            table.addCell(new Cell().add(new Paragraph(quantity.toString())).setFont(font).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(line.unit() != null ? line.unit() : "")).setFont(font).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(unitPrice, invoice.currency()))).setFont(font).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(line.taxRate() != null ? line.taxRate() + "%" : "0%")).setFont(font).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(lineTotal, invoice.currency()))).setFont(font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addInvoiceTotals(Document document, InvoiceDto invoice, PdfFont font, PdfFont boldFont) {
        // Tableau des totaux
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        // Cellule vide à gauche
        totalsTable.addCell(new Cell().setBorder(null));

        // Cellule des totaux à droite
        Cell totalsCell = new Cell()
                .add(new Paragraph("Sous-total HT: " + formatCurrency(invoice.subtotal(), invoice.currency())).setFont(font))
                .add(new Paragraph("TVA: " + formatCurrency(invoice.taxTotal(), invoice.currency())).setFont(font))
                .add(new Paragraph("TOTAL TTC: " + formatCurrency(invoice.total(), invoice.currency())).setFont(boldFont).setFontSize(12))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT);

        totalsTable.addCell(totalsCell);
        
        document.add(totalsTable);
        document.add(new Paragraph("\n"));
    }


    private void addNotesAndLegalMentions(Document document, InvoiceDto invoice, PdfFont font) {
        // Notes
        if (invoice.notes() != null && !invoice.notes().trim().isEmpty()) {
            Paragraph notesTitle = new Paragraph("Notes:")
                    .setFont(font)
                    .setFontSize(10)
                    .setBold();
            document.add(notesTitle);

            Paragraph notes = new Paragraph(invoice.notes())
                    .setFont(font)
                    .setFontSize(9)
                    .setMarginBottom(10);
            document.add(notes);
        }

        // Mentions légales UNIQUEMENT depuis le TaxProfile
        Optional<TaxProfileDto> taxProfile = taxProfileService.getDefaultTaxProfileForFrance();
        if (taxProfile.isPresent() && taxProfile.get().mandatoryMentions() != null && !taxProfile.get().mandatoryMentions().isEmpty()) {
            Paragraph legalTitle = new Paragraph("Mentions légales:")
                    .setFont(font)
                    .setFontSize(10)
                    .setBold();
            document.add(legalTitle);

            for (String mention : taxProfile.get().mandatoryMentions()) {
                Paragraph legalMention = new Paragraph(mention)
                        .setFont(font)
                        .setFontSize(8);
                document.add(legalMention);
            }
        }

        // Conditions de paiement
        if (invoice.paymentTerms() != null && !invoice.paymentTerms().trim().isEmpty()) {
            Paragraph paymentTerms = new Paragraph("Conditions de paiement: " + invoice.paymentTerms())
                    .setFont(font)
                    .setFontSize(9)
                    .setMarginTop(10);
            document.add(paymentTerms);
        }
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        String currencySymbol = currency != null ? currency : "EUR";
        return String.format("%.2f %s", amount, currencySymbol);
    }
}
