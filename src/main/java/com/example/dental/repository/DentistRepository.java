package com.example.dental.repository;

import com.example.dental.entity.Dentist;
import com.example.dental.entity.DentalClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {
    List<Dentist> findByDentalClinic(DentalClinic clinic);
}
