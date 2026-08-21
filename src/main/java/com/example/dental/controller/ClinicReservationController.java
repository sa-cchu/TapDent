package com.example.dental.controller;

import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.DentalClinicRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clinic/reservations")
public class ClinicReservationController {

    private final DentalClinicRepository dentalClinicRepository;

    public ClinicReservationController(DentalClinicRepository dentalClinicRepository) {
        this.dentalClinicRepository = dentalClinicRepository;
    }

    @GetMapping
    public String showReservations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("クリニックが見つかりません"));
        
        model.addAttribute("clinic", clinic);
        return "clinic/reservations/index";
    }
}
