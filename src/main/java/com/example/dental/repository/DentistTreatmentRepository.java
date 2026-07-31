package com.example.dental.repository;

import com.example.dental.entity.DentistTreatment;
import com.example.dental.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentistTreatmentRepository extends JpaRepository<DentistTreatment, Long> {
    List<DentistTreatment> findByDentist(Dentist dentist);
    
    void deleteByDentist(Dentist dentist);
}
