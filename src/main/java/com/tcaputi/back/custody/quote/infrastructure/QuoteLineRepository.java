package com.tcaputi.back.custody.quote.infrastructure;

import com.tcaputi.back.custody.quote.domain.model.QuoteLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteLineRepository extends JpaRepository<QuoteLine, UUID> {
    
    List<QuoteLine> findByQuoteId(UUID quoteId);
    
    void deleteByQuoteId(UUID quoteId);
}
