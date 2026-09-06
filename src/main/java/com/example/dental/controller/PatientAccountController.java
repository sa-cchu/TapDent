package com.example.dental.controller;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Patient;
import com.example.dental.form.PatientAccountForm;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.PatientRepository;
import com.example.dental.service.EmailService;
import com.example.dental.service.PatientService;
import com.example.dental.service.PatientUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/patient/{token}/account")
public class PatientAccountController {

    private final DentalClinicRepository dentalClinicRepository;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final EmailService emailService;

    public PatientAccountController(DentalClinicRepository dentalClinicRepository, 
                                    PatientRepository patientRepository,
                                    PatientService patientService,
                                    EmailService emailService) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.emailService = emailService;
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
    public String showAccountForm(@PathVariable String token, 
                                  @AuthenticationPrincipal PatientUserDetails userDetails, 
                                  Model model) {
        if (!model.containsAttribute("patientAccountForm")) {
            Patient patient = userDetails.getPatient();
            PatientAccountForm form = new PatientAccountForm();
            form.setName(patient.getName());
            form.setPronunciationGuide(patient.getPronunciationGuide());
            form.setBirthday(patient.getBirthday());
            form.setGender(patient.getGender());
            form.setTel(patient.getTel());
            form.setEmail(patient.getEmail());
            model.addAttribute("patientAccountForm", form);
        }
        
        // ログイン時の患者のメールアドレスを保持（JSでの比較用）
        model.addAttribute("originalEmail", userDetails.getPatient().getEmail());

        return "patient/account_edit";
    }

    @PostMapping("/api/send-code")
    @ResponseBody
    public ResponseEntity<?> sendVerificationCode(@PathVariable String token,
                                                  @AuthenticationPrincipal PatientUserDetails userDetails,
                                                  @RequestBody Map<String, String> payload,
                                                  HttpSession session) {
        DentalClinic clinic = dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));
        
        String email = payload.get("email");
        
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "メールアドレスが必要です"));
        }

        // 重複チェック
        if (patientRepository.findByDentalClinicAndEmail(clinic, email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "このメールアドレスは既に登録されています"));
        }

        // 開発・テスト用にパスコードを固定（本番環境では元に戻す）
        // String passcode = emailService.generatePasscode();
        String passcode = "0000";
        
        // セッションに保存
        session.setAttribute("accountVerificationCode", passcode);
        session.setAttribute("accountVerificationCodeExpiry", LocalDateTime.now().plusMinutes(15));
        session.setAttribute("accountVerificationEmail", email); // 紐付け用

        // 非同期でメール送信（開発・テスト用のため一旦停止、またはそのまま呼ぶ）
        // emailService.sendAccountVerificationCode(email, passcode);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping
    public String updateAccount(@PathVariable String token,
                                @AuthenticationPrincipal PatientUserDetails userDetails,
                                @Validated @ModelAttribute PatientAccountForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                HttpSession session) {
                                
        if (bindingResult.hasErrors()) {
            model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
            return "patient/account_edit";
        }

        boolean isEmailChanged = !userDetails.getPatient().getEmail().equals(form.getEmail());

        if (isEmailChanged) {
            String savedCode = (String) session.getAttribute("accountVerificationCode");
            LocalDateTime expiry = (LocalDateTime) session.getAttribute("accountVerificationCodeExpiry");
            String savedEmail = (String) session.getAttribute("accountVerificationEmail");

            if (savedCode == null || expiry == null || savedEmail == null) {
                bindingResult.rejectValue("verificationCode", "error.verificationCode", "認証セッションがタイムアウトしました。最初からやり直してください。");
                model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
                return "patient/account_edit";
            }
            if (LocalDateTime.now().isAfter(expiry)) {
                bindingResult.rejectValue("verificationCode", "error.verificationCode", "認証コードの有効期限が切れています。");
                model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
                return "patient/account_edit";
            }
            if (!savedEmail.equals(form.getEmail())) {
                bindingResult.rejectValue("verificationCode", "error.verificationCode", "認証時のメールアドレスと一致しません。");
                model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
                return "patient/account_edit";
            }
            if (form.getVerificationCode() == null || !form.getVerificationCode().equals(savedCode)) {
                bindingResult.rejectValue("verificationCode", "error.verificationCode", "認証コードが正しくありません。");
                model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
                return "patient/account_edit";
            }
            
            // 認証成功したらセッションから削除
            session.removeAttribute("accountVerificationCode");
            session.removeAttribute("accountVerificationCodeExpiry");
            session.removeAttribute("accountVerificationEmail");
        }

        try {
            patientService.updatePatientInfo(userDetails.getPatient().getPatientId(), form);
            
            // ユーザー情報（セッション）の更新を反映させるためにUserDetailsの患者情報も更新
            Patient updatedPatient = userDetails.getPatient();
            updatedPatient.setName(form.getName());
            updatedPatient.setPronunciationGuide(form.getPronunciationGuide());
            updatedPatient.setBirthday(form.getBirthday());
            updatedPatient.setGender(form.getGender());
            updatedPatient.setTel(form.getTel());
            updatedPatient.setEmail(form.getEmail());
            
            redirectAttributes.addFlashAttribute("successMessage", "アカウント情報を更新しました。");
            return "redirect:/patient/" + token + "/account";
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("メールアドレス")) {
                bindingResult.rejectValue("email", "error.email", e.getMessage());
            } else {
                bindingResult.reject("error.global", e.getMessage());
            }
            model.addAttribute("originalEmail", userDetails.getPatient().getEmail());
            return "patient/account_edit";
        }
    }
}
