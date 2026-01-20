package com.tcaputi.back.custody.invoice.infrastructure;

import com.tcaputi.back.custody.invoice.domain.model.InvoiceSequence;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InvoiceSequence> findByTypeAndYear(InvoiceType type, Integer year);
    
    @Modifying
    @Query("UPDATE InvoiceSequence s SET s.currentNumber = s.currentNumber + 1 WHERE s.id = :id")
    int incrementSequence(@Param("id") Long id);
}
