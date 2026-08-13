package com.example.dental.repository;

import com.example.dental.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDentalClinicAndStartAtBetween(com.example.dental.entity.DentalClinic clinic, LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    java.util.Optional<Appointment> findFirstByPatientPatientIdAndStatusAndStartAtAfterOrderByStartAtAsc(
        Long patientId, com.example.dental.enums.AppointmentStatus status, LocalDateTime now);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    List<Appointment> findByPatientPatientIdAndStatusAndStartAtAfterOrderByStartAtAsc(
        Long patientId, com.example.dental.enums.AppointmentStatus status, LocalDateTime now);
}
