package com.example.dental.repository;

import com.example.dental.entity.DentalClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DentalClinicRepository extends JpaRepository<DentalClinic, Long> {
    Optional<DentalClinic> findByLoginId(String loginId);
    Optional<DentalClinic> findByPublicUrlToken(String publicUrlToken);

    org.springframework.data.domain.Page<DentalClinic> findByNameContainingAndContractStatus(String name, com.example.dental.entity.ContractStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<DentalClinic> findByNameContaining(String name, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<DentalClinic> findByContractStatus(com.example.dental.entity.ContractStatus status, org.springframework.data.domain.Pageable pageable);
}
