package com.example.dental.controller;

import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.entity.Patient;
import com.example.dental.repository.PatientRepository;
import com.example.dental.service.PatientUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/patient/{token}")
public class PatientLoginController {

    private final DentalClinicRepository dentalClinicRepository;
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PatientLoginController(DentalClinicRepository dentalClinicRepository,
                                  PatientRepository patientRepository,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage(@PathVariable String token, 
                                @RequestParam(value = "error", required = false) String error,
                                Model model) {
        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));
        
        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        
        if (error != null) {
            model.addAttribute("errorMessage", "メールアドレスまたはパスワードが間違っています。");
        }
        
        return "patient/login";
    }

    @PostMapping("/login")
    public String processLogin(@PathVariable String token,
                               @RequestParam String email,
                               @RequestParam String password,
                               HttpServletRequest request) {

        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));

        Optional<Patient> patientOpt = patientRepository.findByDentalClinicAndEmail(clinic, email);
        
        // 存在チェックとパスワード照合
        if (patientOpt.isEmpty() || !passwordEncoder.matches(password, patientOpt.get().getPassword())) {
            return "redirect:/patient/" + token + "/login?error=true";
        }
        
        Patient patient = patientOpt.get();
        // ロックや削除のチェック
        if (patient.getIsDeleted() || (patient.getLockedUntill() != null && patient.getLockedUntill().isAfter(LocalDateTime.now()))) {
            return "redirect:/patient/" + token + "/login?error=true";
        }

        // 手動でSpring Securityのコンテキストに認証情報をセット
        PatientUserDetails userDetails = new PatientUserDetails(patient);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // セッションに保存
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        // ダッシュボードへリダイレクト
        return "redirect:/patient/" + token + "/dashboard";
    }
}
