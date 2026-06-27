package com.example.dental.repository;

import com.example.dental.entity.DentalChair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DentalChairRepository extends JpaRepository<DentalChair, Long> {
}
