package com.tcaputi.back.custody.invoice.infrastructure;

import com.tcaputi.back.custody.invoice.exception.MailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from-mail}")
    private String from;

    @Value("${spring.mail.from-name}")
    private String fromName;

    public void sendInvoiceByEmail(String to, String subject, String body, byte[] invoicePdf, String invoiceNumber) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // true = HTML

            // Ajout du PDF en pièce jointe
            String filename = "facture-" + invoiceNumber + ".pdf";
            helper.addAttachment(filename, new ByteArrayResource(invoicePdf), "application/pdf");

            javaMailSender.send(message);
            log.info("Facture {} envoyée avec succès à {}", invoiceNumber, to);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la facture {} à {}: {}",
                    invoiceNumber, to, e.getMessage(), e);
            throw new MailException("Erreur lors de l'envoi de l'email", e);
        } catch (UnsupportedEncodingException e) {
            throw new MailException("Erreur lors de l'encodage du mail", e);
        }
    }

}
