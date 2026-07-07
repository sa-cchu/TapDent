package com.example.dental.controller;

import com.example.dental.dto.DentalClinicDto;
import com.example.dental.service.DentalClinicService;
import com.example.dental.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/clinic/settings")
public class ClinicSettingsController {

    private final DentalClinicService dentalClinicService;
    private final EmailService emailService;

    public ClinicSettingsController(DentalClinicService dentalClinicService, EmailService emailService) {
        this.dentalClinicService = dentalClinicService;
        this.emailService = emailService;
    }

    @GetMapping
    public String settingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DentalClinicDto clinic = dentalClinicService.getClinicByLoginId(userDetails.getUsername());
        model.addAttribute("clinic", clinic);
        return "clinic/settings";
    }

    @PostMapping("/request-passcode")
    @ResponseBody
    public ResponseEntity<Map<String, String>> requestPasscode(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            DentalClinicDto clinic = dentalClinicService.getClinicByLoginId(userDetails.getUsername());
            String email = clinic.getMail();

            if (email == null || email.isBlank()) {
                response.put("status", "error");
                response.put("message", "メールアドレスが登録されていません。");
                return ResponseEntity.badRequest().body(response);
            }

            String passcode = emailService.generatePasscode();
            session.setAttribute("SETTINGS_PASSCODE", passcode);
            
            // 5分間のみ有効（300秒）
            session.setMaxInactiveInterval(300);

            emailService.sendPasscode(email, passcode);

            response.put("status", "success");
            response.put("message", "登録メールアドレスにパスコードを送信しました。");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "エラーが発生しました。");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateCredentials(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String newLoginId,
            @RequestParam String newPassword,
            @RequestParam String passcode,
            HttpSession session,
            HttpServletRequest request) {

        Map<String, String> response = new HashMap<>();
        String savedPasscode = (String) session.getAttribute("SETTINGS_PASSCODE");

        if (savedPasscode == null) {
            response.put("status", "error");
            response.put("message", "パスコードの有効期限が切れているか、発行されていません。");
            return ResponseEntity.badRequest().body(response);
        }

        if (!savedPasscode.equals(passcode)) {
            response.put("status", "error");
            response.put("message", "パスコードが正しくありません。");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // パスコード一致したので更新
            dentalClinicService.updateCredentials(userDetails.getUsername(), newLoginId, newPassword);

            // セッション破棄（ログアウトさせるため）
            session.invalidate();
            
            response.put("status", "success");
            response.put("message", "認証情報を更新しました。再度ログインしてください。");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "更新に失敗しました。IDが既に使われている可能性があります。");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
