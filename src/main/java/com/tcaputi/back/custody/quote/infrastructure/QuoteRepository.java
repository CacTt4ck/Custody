package com.tcaputi.back.custody.quote.infrastructure;

import com.tcaputi.back.custody.quote.domain.model.Quote;
import com.tcaputi.back.custody.quote.domain.model.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    
    Page<Quote> findByClientId(UUID clientId, Pageable pageable);
    
    Page<Quote> findByProjectId(UUID projectId, Pageable pageable);
    
    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);
    
    List<Quote> findByValidUntilBefore(LocalDate date);
    
    @Query("SELECT q FROM Quote q WHERE q.client.id = :clientId AND q.status = :status")
    List<Quote> findByClientIdAndStatus(@Param("clientId") UUID clientId, @Param("status") QuoteStatus status);
    
    @Query("SELECT q FROM Quote q WHERE q.number LIKE %:number%")
    List<Quote> findByNumberContaining(@Param("number") String number);

    int countByStatus(QuoteStatus status);
}
