package com.example.dental.repository;

import com.example.dental.entity.TreatmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.example.dental.entity.DentalClinic;

@Repository
public interface TreatmentTypeRepository extends JpaRepository<TreatmentType, Long> {
    List<TreatmentType> findByDentalClinic(DentalClinic clinic);
    List<TreatmentType> findByDentalClinicAndTargetPatientType(DentalClinic clinic, com.example.dental.enums.TargetPatientType targetPatientType);
}
