package com.example.dental.controller;

import com.example.dental.entity.Appointment;
import com.example.dental.entity.DentalChair;
import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.DentalChairRepository;
import com.example.dental.repository.DentalClinicRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clinic/api/reservations")
public class ClinicCalendarRestController {

    private final AppointmentRepository appointmentRepository;
    private final DentalChairRepository dentalChairRepository;
    private final DentalClinicRepository dentalClinicRepository;
    private final com.example.dental.service.DentalClinicService dentalClinicService;

    public ClinicCalendarRestController(AppointmentRepository appointmentRepository,
                                        DentalChairRepository dentalChairRepository,
                                        DentalClinicRepository dentalClinicRepository,
                                        com.example.dental.service.DentalClinicService dentalClinicService) {
        this.appointmentRepository = appointmentRepository;
        this.dentalChairRepository = dentalChairRepository;
        this.dentalClinicRepository = dentalClinicRepository;
        this.dentalClinicService = dentalClinicService;
    }

    private DentalClinic getClinic(UserDetails userDetails) {
        return dentalClinicRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("クリニックが見つかりません"));
    }

    @GetMapping("/business-hours")
    public ResponseEntity<?> getBusinessHours(@AuthenticationPrincipal UserDetails userDetails) {
        DentalClinic clinic = getClinic(userDetails);
        return ResponseEntity.ok(dentalClinicService.getBusinessHours(clinic.getDentalId()));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        DentalClinic clinic = getClinic(userDetails);
        
        // start日の00:00:00から、end日の23:59:59までを取得
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        
        List<Appointment> appointments = appointmentRepository.findByDentalClinicAndStartAtBetween(clinic, startDateTime, endDateTime);
        
        List<Map<String, Object>> response = appointments.stream().map(a -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("appointmentId", a.getAppointmentId());
            map.put("patientName", a.getPatient().getName());
            map.put("patientNameKana", a.getPatient().getPronunciationGuide());
            map.put("patientCode", a.getPatient().getPatientCode());
            map.put("startAt", a.getStartAt() != null ? a.getStartAt().toString() : null);
            map.put("endAt", a.getEndAt() != null ? a.getEndAt().toString() : null);
            map.put("status", a.getStatus().name());
            map.put("visitType", a.getVisitType().name());
            map.put("treatmentName", a.getTreatmentType().getTreatmentName());
            map.put("appointMethod", a.getAppointMethod().name());
            map.put("chairId", a.getDentalChair().getChairId());
            map.put("chairName", a.getDentalChair().getChairName());
            map.put("dentistName", a.getDentist().getDentistName());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chairs")
    public ResponseEntity<?> getChairs(@AuthenticationPrincipal UserDetails userDetails) {
        DentalClinic clinic = getClinic(userDetails);
        List<DentalChair> chairs = dentalChairRepository.findByDentalClinicAndIsDeletedFalse(clinic);
        
        List<Map<String, Object>> response = chairs.stream().map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("chairId", c.getChairId());
            map.put("chairName", c.getChairName());
            map.put("status", c.getStatus());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
