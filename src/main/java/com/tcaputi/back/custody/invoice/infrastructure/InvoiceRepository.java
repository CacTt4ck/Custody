package com.tcaputi.back.custody.invoice.infrastructure;

import com.tcaputi.back.custody.invoice.domain.model.Invoice;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // Recherche par client
    Page<Invoice> findByClientId(UUID clientId, Pageable pageable);
    List<Invoice> findByClientId(UUID clientId);

    // Recherche par projet
    Page<Invoice> findByProjectId(UUID projectId, Pageable pageable);
    List<Invoice> findByProjectId(UUID projectId);

    // Recherche par statut
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    List<Invoice> findByStatus(InvoiceStatus status);

    // Recherche par numéro (unique)
    Optional<Invoice> findByNumber(String number);
    boolean existsByNumber(String number);
    boolean existsByNumberAndIdNot(String number, UUID id);

    // Recherche par période
    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    List<Invoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    // Factures en retard
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    // Factures en retard
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    int countOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    // Recherche multicritères
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:clientId IS NULL OR i.client.id = :clientId) AND " +
           "(:projectId IS NULL OR i.project.id = :projectId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:number IS NULL OR UPPER(i.number) LIKE UPPER(CONCAT('%', :number, '%')))")
    List<Invoice> findInvoicesByMultipleCriteria(
            @Param("clientId") UUID clientId,
            @Param("projectId") UUID projectId,
            @Param("status") InvoiceStatus status,
            @Param("number") String number);

    // Statistiques
    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status = 'PAID' AND i.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") InvoiceStatus status);

    /**
     * Trouve le dernier numéro de facture selon un pattern
     * pour garantir la continuité de numérotation
     */
    @Query("SELECT i.number FROM Invoice i WHERE i.number LIKE :pattern ORDER BY i.number DESC")
    Optional<String> findLastInvoiceNumberByPattern(@Param("pattern") String pattern);

}
