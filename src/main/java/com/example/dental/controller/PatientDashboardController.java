package com.example.dental.controller;

import com.example.dental.entity.Appointment;
import com.example.dental.entity.DentalClinic;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.service.PatientUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/patient/{token}")
public class PatientDashboardController {

    private final DentalClinicRepository dentalClinicRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientDashboardController(DentalClinicRepository dentalClinicRepository,
                                      AppointmentRepository appointmentRepository) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.appointmentRepository = appointmentRepository;
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
            
            // ログイン中の患者の、未来の有効な予約（すべて）を取得
            java.util.List<Appointment> upcomingList = appointmentRepository
                .findByPatientPatientIdAndStatusAndStartAtAfterOrderByStartAtAsc(
                    userDetails.getPatient().getPatientId(), 
                    AppointmentStatus.RESERVED, 
                    LocalDateTime.now()
                );

            if (!upcomingList.isEmpty()) {
                model.addAttribute("hasReservation", true);
                model.addAttribute("upcomingList", upcomingList);
            } else {
                model.addAttribute("hasReservation", false);
            }
        } else {
            model.addAttribute("hasReservation", false);
        }

        return "patient/dashboard";
    }
}
