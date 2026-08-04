package com.example.dental.repository;

import com.example.dental.entity.CalendarException;
import com.example.dental.entity.DentalClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarExceptionRepository extends JpaRepository<CalendarException, Long> {
    
    List<CalendarException> findByDentalClinicAndTargetDateBetween(DentalClinic clinic, LocalDate start, LocalDate end);
    
    List<CalendarException> findByDentalClinicAndDentistIsNullAndTargetDateBetween(DentalClinic clinic, LocalDate start, LocalDate end);
    
    @Query("SELECT c FROM CalendarException c WHERE c.dentalClinic = :clinic AND c.dentist.dentistId = :dentistId AND c.targetDate BETWEEN :start AND :end")
    List<CalendarException> findExceptionsByDentist(
        @Param("clinic") DentalClinic clinic, 
        @Param("dentistId") Long dentistId, 
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end
    );
}

