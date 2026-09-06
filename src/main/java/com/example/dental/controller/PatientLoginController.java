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
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/patient/" + token + "/dashboard";
        }

        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));
        
        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        
        if (error != null) {
            if ("locked".equals(error)) {
                model.addAttribute("errorMessage", "アカウントが一時的にロックされています。30分後に再度お試しください。");
            } else if ("frozen".equals(error)) {
                model.addAttribute("errorMessage", "アカウントが凍結されています。医院へお問い合わせください。");
            } else {
                model.addAttribute("errorMessage", "メールアドレスまたはパスワードが間違っています。");
            }
        }
        
        return "patient/login";
    }

    @PostMapping("/login")
    public String processLogin(@PathVariable String token,
                               @RequestParam String loginId,
                               @RequestParam String password,
                               HttpServletRequest request) {

        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));

        Optional<Patient> patientOpt = patientRepository.findByClinicAndLoginId(clinic, loginId);
        
        if (patientOpt.isEmpty()) {
            return "redirect:/patient/" + token + "/login?error=true";
        }
        
        Patient patient = patientOpt.get();

        // 削除チェック
        if (patient.getIsDeleted()) {
            return "redirect:/patient/" + token + "/login?error=true";
        }

        // 凍結チェック
        if (patient.getStatus() == com.example.dental.enums.PatientStatus.WITHDRAWN) {
            return "redirect:/patient/" + token + "/login?error=frozen";
        }
        
        // ロックチェック（現在時刻がロック解除時刻より前ならロック中）
        if (patient.getLockedUntill() != null && patient.getLockedUntill().isAfter(LocalDateTime.now())) {
            return "redirect:/patient/" + token + "/login?error=locked";
        }

        // パスワード照合
        if (!passwordEncoder.matches(password, patient.getPassword())) {
            // 失敗回数をカウントアップ
            patient.setLoginAttempts(patient.getLoginAttempts() + 1);
            if (patient.getLoginAttempts() >= 5) {
                // 5回失敗で30分間ロック（実務的な最適解）
                patient.setLockedUntill(LocalDateTime.now().plusMinutes(30));
                patientRepository.save(patient);
                return "redirect:/patient/" + token + "/login?error=locked";
            }
            patientRepository.save(patient);
            return "redirect:/patient/" + token + "/login?error=true";
        }
        
        // ログイン成功時：失敗回数とロック情報をリセット
        patient.setLoginAttempts(0);
        patient.setLockedUntill(null);
        patientRepository.save(patient);

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

    @GetMapping("/logout")
    public String logout(@PathVariable String token, HttpServletRequest request) {
        // セッションからSpring Securityのコンテキストをクリアし、セッションを破棄
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // ログアウト完了後、ログイン画面へリダイレクト
        return "redirect:/patient/" + token + "/login?logout=true";
    }
}
