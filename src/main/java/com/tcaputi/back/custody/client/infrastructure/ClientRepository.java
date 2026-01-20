package com.tcaputi.back.custody.client.infrastructure;

import com.tcaputi.back.custody.client.domain.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query(value = "SELECT * FROM clients c WHERE " +
            "(:email IS NULL OR UPPER(c.contact_email) LIKE UPPER('%' || :email || '%')) AND " +
            "(:legalName IS NULL OR UPPER(c.legal_name) LIKE UPPER('%' || :legalName || '%')) AND " +
            "(:contactName IS NULL OR UPPER(c.contact_name) LIKE UPPER('%' || :contactName || '%'))",
            nativeQuery = true)
    List<Client> findClientsByMultipleCriteria(
            @Param("email") String email,
            @Param("legalName") String legalName,
            @Param("contactName") String contactName
    );



}
