package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Provider;
import com.lasystems.lagenda.models.ServiceSpecialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ServiceSpecialtiesRepository extends JpaRepository<ServiceSpecialties, UUID>, JpaSpecificationExecutor<ServiceSpecialties> {
}
