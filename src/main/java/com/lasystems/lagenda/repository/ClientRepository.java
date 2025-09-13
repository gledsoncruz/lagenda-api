package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

//    @Query("""
//    SELECT c.id as id, c.name as name, c.email as email, c.phone as phone, comp as company
//    FROM Client c
//    JOIN c.company comp
//    ORDER BY c.name
//    """)
//    List<ClientDto> findClientDto();

//    @Query("""
//        SELECT
//            c.id as id,
//            c.name as name,
//            c.email as email,
//            c.phone as phone,
//            c.conversationHistory,
//            a as appointments
//        FROM Client c
//        LEFT JOIN c.appointments a
//        WHERE c.phone = :phone
//        ORDER BY c.name
//    """)
//    ClientDto findByPhone(@Param("phone") String phone);
    @EntityGraph(attributePaths = {"appointments"})
    Optional<Client> findByPhone(String phone);

}
