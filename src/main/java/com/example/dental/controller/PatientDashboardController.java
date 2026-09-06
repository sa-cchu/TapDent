package com.example.dental.controller;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.dental.entity.Appointment;
import com.example.dental.entity.DentalClinic;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.AppointmentHistoryRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.service.PatientUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/patient/{token}")
public class PatientDashboardController {

    private final DentalClinicRepository dentalClinicRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository appointmentHistoryRepository;

    public PatientDashboardController(DentalClinicRepository dentalClinicRepository,
                                      AppointmentRepository appointmentRepository,
                                      AppointmentHistoryRepository appointmentHistoryRepository) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentHistoryRepository = appointmentHistoryRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@PathVariable String token, 
                                @AuthenticationPrincipal PatientUserDetails userDetails,
                                Model model) {
        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));

        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        
        if (userDetails != null) {
            model.addAttribute("patientName", userDetails.getPatient().getName());
            model.addAttribute("patientEmail", userDetails.getPatient().getEmail());
            
            // ログイン中の患者の、アクセスした日以降（本日0時以降）の有効な予約（すべて）を取得
            java.util.List<Appointment> upcomingList = appointmentRepository
                .findByPatientPatientIdAndStatusAndStartAtGreaterThanEqualOrderByStartAtAsc(
                    userDetails.getPatient().getPatientId(), 
                    AppointmentStatus.RESERVED, 
                    LocalDate.now(java.time.ZoneId.of("Asia/Tokyo")).atStartOfDay()
                );

            if (!upcomingList.isEmpty()) {
                model.addAttribute("hasReservation", true);
                model.addAttribute("upcomingList", upcomingList);
            } else {
                model.addAttribute("hasReservation", false);
            }
            
            // 最新の過去の予約（アクセスした日より前）を1件取得
            appointmentRepository.findFirstByPatientPatientIdAndStatusInAndStartAtLessThanOrderByStartAtDesc(
                    userDetails.getPatient().getPatientId(), 
                    java.util.Arrays.asList(AppointmentStatus.RESERVED, AppointmentStatus.ATTENDED),
                    LocalDate.now(java.time.ZoneId.of("Asia/Tokyo")).atStartOfDay())
                .ifPresent(history -> {
                    model.addAttribute("hasHistory", true);
                    model.addAttribute("lastHistory", history);
                });
        } else {
            model.addAttribute("hasReservation", false);
        }

        return "patient/dashboard";
    }
}
