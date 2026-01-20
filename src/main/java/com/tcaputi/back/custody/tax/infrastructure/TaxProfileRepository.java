package com.tcaputi.back.custody.tax.infrastructure;

import com.tcaputi.back.custody.tax.domain.model.TaxProfile;
import com.tcaputi.back.custody.tax.domain.model.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxProfileRepository extends JpaRepository<TaxProfile, UUID> {

    Optional<TaxProfile> findFirstByType(TaxType type);

    List<TaxProfile> findAllByType(TaxType type);
}