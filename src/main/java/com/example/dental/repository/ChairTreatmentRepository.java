package com.example.dental.repository;

import com.example.dental.entity.ChairTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.dental.entity.DentalChair;

@Repository
public interface ChairTreatmentRepository extends JpaRepository<ChairTreatment, Long> {
    List<ChairTreatment> findByDentalChair(DentalChair chair);
    
    @Modifying
    @Query("DELETE FROM ChairTreatment ct WHERE ct.dentalChair = :chair")
    void deleteByDentalChair(@Param("chair") DentalChair chair);
}
