package com.example.dental.controller;

import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.DentalClinicRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final DentalClinicRepository dentalClinicRepository;

    public PatientController(DentalClinicRepository dentalClinicRepository) {
        this.dentalClinicRepository = dentalClinicRepository;
    }

    /**
     * 初診・再診の選択画面（患者向けの初期画面）
     * URL: /patient/{token}
     */
    @GetMapping("/{token}")
    public String showIndex(@PathVariable("token") String token, Model model) {
        Optional<DentalClinic> clinicOpt = dentalClinicRepository.findByPublicUrlToken(token);
        
        if (clinicOpt.isEmpty()) {
            // トークンに合致する医院が見つからない場合
            return "error/404"; 
        }
        
        DentalClinic clinic = clinicOpt.get();
        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        
        return "patient/index";
    }
}
