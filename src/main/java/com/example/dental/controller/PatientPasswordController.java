package com.example.dental.controller;

import com.example.dental.entity.DentalClinic;
import com.example.dental.form.PatientPasswordForm;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.service.PatientService;
import com.example.dental.service.PatientUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patient/{token}/password")
public class PatientPasswordController {

    private final DentalClinicRepository dentalClinicRepository;
    private final PatientService patientService;
    private final PasswordEncoder passwordEncoder;
    private final com.example.dental.service.SystemLogService systemLogService;

    public PatientPasswordController(DentalClinicRepository dentalClinicRepository, 
                                     PatientService patientService,
                                     PasswordEncoder passwordEncoder,
                                     com.example.dental.service.SystemLogService systemLogService) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.patientService = patientService;
        this.passwordEncoder = passwordEncoder;
        this.systemLogService = systemLogService;
    }

    @ModelAttribute
    public void addAttributes(@PathVariable String token, @AuthenticationPrincipal PatientUserDetails userDetails, Model model) {
        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));
        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        
        if (userDetails != null) {
            model.addAttribute("patientName", userDetails.getPatient().getName());
            model.addAttribute("patientEmail", userDetails.getPatient().getEmail());
        }
    }

    @GetMapping
    public String showPasswordForm(@PathVariable String token, Model model) {
        if (!model.containsAttribute("patientPasswordForm")) {
            model.addAttribute("patientPasswordForm", new PatientPasswordForm());
        }
        return "patient/password_edit";
    }

    @PostMapping
    public String updatePassword(@PathVariable String token,
                                 @AuthenticationPrincipal PatientUserDetails userDetails,
                                 @Validated @ModelAttribute PatientPasswordForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 jakarta.servlet.http.HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "patient/password_edit";
        }

        // 現在のパスワード確認
        if (!passwordEncoder.matches(form.getCurrentPassword(), userDetails.getPassword())) {
            bindingResult.rejectValue("currentPassword", "error.currentPassword", "現在のパスワードが正しくありません。");
            return "patient/password_edit";
        }

        // 新しいパスワードの一致確認
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "新しいパスワードが一致しません。");
            return "patient/password_edit";
        }

        // パスワード更新
        patientService.updatePatientPassword(userDetails.getPatient().getPatientId(), form.getNewPassword());
        
        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token).orElse(null);
        systemLogService.saveLog(com.example.dental.enums.LogActionType.PASSWORD_CHANGE, userDetails.getUsername(), clinic, "患者パスワード変更", request);

        // セッション内のパスワード情報も更新
        userDetails.getPatient().setPassword(passwordEncoder.encode(form.getNewPassword()));

        redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。");
        // パスワード変更後はダッシュボードまたはアカウント情報画面へリダイレクト
        return "redirect:/patient/" + token + "/dashboard";
    }
}
