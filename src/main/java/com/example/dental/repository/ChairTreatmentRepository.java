package com.example.dental.repository;

import com.example.dental.entity.ChairTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChairTreatmentRepository extends JpaRepository<ChairTreatment, Long> {
}
