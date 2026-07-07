package com.example.dental.controller;

import com.example.dental.dto.DentalClinicDto;
import com.example.dental.service.DentalClinicService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 歯科医院(Clinic)用コントローラー
 * - ログイン画面 (GET /clinic/login)
 * - ダッシュボード (GET /clinic/dashboard)
 */
@Controller
@RequestMapping("/clinic")
public class ClinicController {

    private final DentalClinicService dentalClinicService;

    public ClinicController(DentalClinicService dentalClinicService) {
        this.dentalClinicService = dentalClinicService;
    }

    /**
     * ログイン画面を表示する。
     * Spring Security の formLogin 設定によりログイン処理自体は自動的に行われる。
     */
    @GetMapping("/login")
    public String loginPage() {
        return "clinic/login";
    }

    /**
     * ログイン成功後に表示するダッシュボード。
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DentalClinicDto clinic = dentalClinicService.getClinicByLoginId(userDetails.getUsername());
        model.addAttribute("clinic", clinic);
        return "clinic/dashboard";
    }
}
