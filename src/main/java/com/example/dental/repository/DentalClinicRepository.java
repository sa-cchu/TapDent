package com.example.dental.repository;

import com.example.dental.entity.DentalClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface DentalClinicRepository extends JpaRepository<DentalClinic, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    Optional<DentalClinic> findByLoginId(String loginId);
    Optional<DentalClinic> findByPublicUrlToken(String publicUrlToken);

    org.springframework.data.domain.Page<DentalClinic> findByNameContainingAndContractStatus(String name, com.example.dental.enums.ContractStatusName status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<DentalClinic> findByNameContaining(String name, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<DentalClinic> findByContractStatus(com.example.dental.enums.ContractStatusName status, org.springframework.data.domain.Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DentalClinic d WHERE d.dentalId = :id")
    Optional<DentalClinic> findByIdForUpdate(@Param("id") Long id);
}
