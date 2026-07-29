package com.example.dental.repository;

import com.example.dental.entity.DentalChair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.example.dental.entity.DentalClinic;

@Repository
public interface DentalChairRepository extends JpaRepository<DentalChair, Long> {
    List<DentalChair> findByDentalClinic(DentalClinic clinic);
}
