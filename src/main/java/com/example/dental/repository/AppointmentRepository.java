package com.example.dental.repository;

import com.example.dental.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDentalClinicAndStartAtBetween(com.example.dental.entity.DentalClinic clinic, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDentalClinicAndStartAtBetweenAndStatusIn(com.example.dental.entity.DentalClinic clinic, LocalDateTime start, LocalDateTime end, java.util.Collection<com.example.dental.enums.AppointmentStatus> statuses);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    java.util.Optional<Appointment> findFirstByPatientPatientIdAndStatusAndStartAtAfterOrderByStartAtAsc(
        Long patientId, com.example.dental.enums.AppointmentStatus status, LocalDateTime now);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    List<Appointment> findByPatientPatientIdAndStatusAndStartAtAfterOrderByStartAtAsc(
        Long patientId, com.example.dental.enums.AppointmentStatus status, LocalDateTime now);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    List<Appointment> findByPatientPatientIdAndStatusAndStartAtGreaterThanEqualOrderByStartAtAsc(
        Long patientId, com.example.dental.enums.AppointmentStatus status, LocalDateTime date);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    java.util.Optional<Appointment> findFirstByPatientPatientIdAndStatusInAndStartAtLessThanOrderByStartAtDesc(
        Long patientId, java.util.Collection<com.example.dental.enums.AppointmentStatus> statuses, LocalDateTime date);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    List<Appointment> findByPatientPatientIdOrderByStartAtDesc(Long patientId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"treatmentType", "dentist"})
    List<Appointment> findByTokenTokenIdOrderByStartAtDesc(Long tokenId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT a FROM Appointment a " +
        "LEFT JOIN a.patient p " +
        "LEFT JOIN a.token t " +
        "WHERE a.dentalClinic.dentalId = :clinicId AND a.status != com.example.dental.enums.AppointmentStatus.CANCELLED AND (" +
        "  p.name LIKE %:keyword% OR p.pronunciationGuide LIKE %:keyword% OR p.tel LIKE %:keyword% OR p.patientCode LIKE %:keyword% OR " +
        "  t.name LIKE %:keyword% OR t.nameKana LIKE %:keyword% OR t.tell LIKE %:keyword%" +
        ") ORDER BY a.startAt DESC"
    )
    List<Appointment> searchAppointmentsByKeyword(@org.springframework.data.repository.query.Param("clinicId") Long clinicId, @org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
}
