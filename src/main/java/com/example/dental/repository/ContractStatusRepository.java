package com.example.dental.repository;

import com.example.dental.entity.ContractStatus;
import com.example.dental.enums.ContractStatusName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractStatusRepository extends JpaRepository<ContractStatus, Integer> {
    Optional<ContractStatus> findByStatusName(ContractStatusName statusName);
}
