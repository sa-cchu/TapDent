package com.example.dental.controller;

import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.TreatmentType;
import com.example.dental.enums.TargetPatientType;
import com.example.dental.form.PatientReservationForm;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.PatientRepository;
import com.example.dental.repository.TreatmentTypeRepository;
import com.example.dental.service.AppointmentService;
import com.example.dental.service.EmailService;
import com.example.dental.service.PatientReservationService;
import com.example.dental.service.PatientUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reserve/{token}")
public class PatientReservationController {

    private final DentalClinicRepository dentalClinicRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;
    private final AppointmentService appointmentService;
    private final PatientReservationService patientReservationService;
    private final EmailService emailService;
    private final PatientRepository patientRepository;
    private final com.example.dental.service.SystemLogService systemLogService;

    public PatientReservationController(DentalClinicRepository dentalClinicRepository,
                                        TreatmentTypeRepository treatmentTypeRepository,
                                        AppointmentService appointmentService,
                                        PatientReservationService patientReservationService,
                                        EmailService emailService,
                                        PatientRepository patientRepository,
                                        com.example.dental.service.SystemLogService systemLogService) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.appointmentService = appointmentService;
        this.patientReservationService = patientReservationService;
        this.emailService = emailService;
        this.patientRepository = patientRepository;
        this.systemLogService = systemLogService;
    }

    private DentalClinic getClinicOrThrow(String token) {
        return dentalClinicRepository.findByPublicUrlToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効な医院トークンです"));
    }

    // --- SPA HTMLの提供 ---
    @GetMapping
    public String showReservationPage(@PathVariable String token, Model model) {
        DentalClinic clinic = getClinicOrThrow(token);
        model.addAttribute("clinic", clinic);
        model.addAttribute("token", token);
        return "patient/reserve/index"; // SPA用の統合HTML
    }

    // --- API: 初期データの取得 ---
    @GetMapping("/api/init")
    @ResponseBody
    public Map<String, Object> getInitData(@PathVariable String token, @AuthenticationPrincipal PatientUserDetails userDetails) {
        DentalClinic clinic = getClinicOrThrow(token);
        
        TargetPatientType targetType = (userDetails != null) ? TargetPatientType.EXISTING : TargetPatientType.FIRST_VISIT;
        
        List<TreatmentType> treatments = treatmentTypeRepository.findByDentalClinicAndIsDeletedFalse(clinic)
                .stream()
                .filter(t -> t.getTargetPatientType() == targetType)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("clinicName", clinic.getName());
        response.put("treatments", treatments);
        response.put("maxReserveMonth", clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3);
        response.put("reservationRestrictions", clinic.getReservationRestrictions());
        
        if (userDetails != null) {
            Map<String, String> patientData = new HashMap<>();
            patientData.put("name", userDetails.getPatient().getName());
            patientData.put("kana", userDetails.getPatient().getPronunciationGuide());
            patientData.put("birthday", userDetails.getPatient().getBirthday().toString());
            patientData.put("gender", userDetails.getPatient().getGender());
            patientData.put("tel", userDetails.getPatient().getTel());
            patientData.put("email", userDetails.getPatient().getEmail());
            if (userDetails.getPatient().getPatientCode() != null) {
                patientData.put("patientCode", userDetails.getPatient().getPatientCode());
            }
            response.put("patient", patientData);
        }
        
        return response;
    }

    // --- API: カレンダー状態の取得 ---
    @GetMapping("/api/calendar")
    @ResponseBody
    public Map<String, String> getCalendarStatus(@PathVariable String token,
                                                    @RequestParam("year") int year,
                                                    @RequestParam("month") int month,
                                                    @RequestParam("treatmentId") Long treatmentId) {
        DentalClinic clinic = getClinicOrThrow(token);
        TreatmentType treatment = treatmentTypeRepository.findById(treatmentId).orElseThrow();
        
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        int maxMonths = clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3;
        LocalDate maxAllowedDate = today.plusMonths(maxMonths);
        
        Map<String, String> statusMap = new HashMap<>();
        
        for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            String dateStr = date.toString();
            if (date.isBefore(today)) {
                statusMap.put(dateStr, "-");
                continue;
            }
            if (date.isAfter(maxAllowedDate)) {
                statusMap.put(dateStr, "-");
                continue;
            }
            if (date.isEqual(today) && clinic.getReservationRestrictions() != null && clinic.getReservationRestrictions()) {
                statusMap.put(dateStr, "-");
                continue;
            }
            
            if (appointmentService.isClinicHoliday(clinic, date)) {
                statusMap.put(dateStr, "-");
                continue;
            }
            
            List<LocalTime> slots = appointmentService.getAvailableTimeSlots(clinic, date, treatment);
            if (date.isEqual(today)) {
                LocalTime now = LocalTime.now(java.time.ZoneId.of("Asia/Tokyo"));
                slots = slots.stream().filter(time -> time.isAfter(now)).collect(Collectors.toList());
            }
            
            if (slots.isEmpty()) {
                statusMap.put(dateStr, "×");
            } else {
                statusMap.put(dateStr, "○");
            }
        }
        
        return statusMap;
    }

    // --- API: 空き枠の取得 ---
    @GetMapping("/api/slots")
    @ResponseBody
    public List<String> getAvailableSlots(@PathVariable String token,
                                          @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                          @RequestParam("treatmentId") Long treatmentId) {
        DentalClinic clinic = getClinicOrThrow(token);
        TreatmentType treatment = treatmentTypeRepository.findById(treatmentId).orElseThrow();
        
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        if (date.isBefore(today)) {
            return List.of();
        }
        if (clinic.getReservationRestrictions() != null && clinic.getReservationRestrictions() && date.isEqual(today)) {
            return List.of();
        }
        int maxMonths = clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3;
        LocalDate maxAllowedDate = today.plusMonths(maxMonths);
        if (date.isAfter(maxAllowedDate)) {
            return List.of();
        }

        List<LocalTime> slots = appointmentService.getAvailableTimeSlots(clinic, date, treatment);
        
        if (date.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            slots = slots.stream().filter(time -> time.isAfter(now)).collect(Collectors.toList());
        }

        return slots.stream().map(LocalTime::toString).collect(Collectors.toList());
    }

    // --- API: 認証コードの送信 ---
    @PostMapping("/api/send-code")
    @ResponseBody
    public ResponseEntity<?> sendVerificationCode(@PathVariable String token,
                                                  @RequestBody Map<String, String> payload,
                                                  HttpSession session) {
        DentalClinic clinic = getClinicOrThrow(token);
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
        session.setAttribute("verificationCode", passcode);
        session.setAttribute("verificationCodeExpiry", LocalDateTime.now().plusMinutes(15));
        session.setAttribute("verificationEmail", email); // 紐付け用

        // 非同期でメール送信（開発・テスト用のため一旦停止）
        // emailService.sendReservationVerificationCode(email, passcode);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // --- API: 予約の確定（新規／メール変更あり） ---
    @PostMapping("/api/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmReservation(@PathVariable String token,
                                                @AuthenticationPrincipal PatientUserDetails userDetails,
                                                @RequestBody @Validated({
                                                    PatientReservationForm.Step1.class,
                                                    PatientReservationForm.Step2.class,
                                                    PatientReservationForm.Step3.class,
                                                    PatientReservationForm.Step4.class
                                                }) PatientReservationForm form,
                                                BindingResult result,
                                                HttpSession session,
                                                jakarta.servlet.http.HttpServletRequest request) {
        DentalClinic clinic = getClinicOrThrow(token);

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", String.join("\n", errors)));
        }

        String savedCode = (String) session.getAttribute("verificationCode");
        LocalDateTime expiry = (LocalDateTime) session.getAttribute("verificationCodeExpiry");
        String savedEmail = (String) session.getAttribute("verificationEmail");

        if (savedCode == null || expiry == null || savedEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "セッションがタイムアウトしました。最初からやり直してください。"));
        }

        if (!savedEmail.equals(form.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "認証時のメールアドレスと一致しません。"));
        }

        if (!form.getInputVerificationCode().equals(savedCode)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "認証コードが正しくありません。"));
        }

        if (LocalDateTime.now().isAfter(expiry)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "認証コードの有効期限（15分）が切れています。最初からやり直してください。"));
        }

        // 不正日時チェック・予約制限チェック
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        int maxMonths = clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3;
        LocalDate maxAllowedDate = today.plusMonths(maxMonths);

        if (form.getReservationDate().isBefore(today)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "過去の日は予約できません。"));
        }
        if (form.getReservationDate().isEqual(today) && form.getReservationTime().isBefore(LocalTime.now(java.time.ZoneId.of("Asia/Tokyo")))) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "過去の時間は予約できません。"));
        }
        if (form.getReservationDate().isAfter(maxAllowedDate)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "予約可能期間（" + maxMonths + "ヶ月先まで）を超えています。"));
        }
        if (clinic.getReservationRestrictions() != null && clinic.getReservationRestrictions() && form.getReservationDate().isEqual(today)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "当日のご予約は受け付けておりません。"));
        }

        // 複合制限（同一医院内で同じ電話番号の二重登録を防ぐ）
        java.util.Optional<com.example.dental.entity.Patient> duplicatePatientOpt = patientRepository.findByDentalClinicAndTel(clinic, form.getTel());
        if (duplicatePatientOpt.isPresent()) {
            if (userDetails == null || !duplicatePatientOpt.get().getPatientId().equals(userDetails.getPatient().getPatientId())) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "この電話番号は既に登録されています。既存のアカウントでログインしてからご予約ください。"));
            }
        }


        TreatmentType treatment = treatmentTypeRepository.findById(form.getTreatmentId()).orElseThrow();

        try {
            com.example.dental.entity.Appointment appt;
            if (userDetails != null) {
                // 既存患者（メールアドレス変更などにより認証を再度行った場合）は既存データを更新
                appt = patientReservationService.createAppointmentForExistingPatient(form, clinic, treatment, userDetails.getPatient());
            } else {
                // 完全な新規患者の場合
                appt = patientReservationService.registerPatientAndAppointment(form, clinic, treatment);
            }
            
            // 予約完了メール送信
            emailService.sendReservationCompleteEmail(form.getEmail(), appt);
            
            if (userDetails == null) {
                systemLogService.saveLog(com.example.dental.enums.LogActionType.USER_CREATE, form.getEmail(), clinic, "患者新規登録 (ID: " + appt.getPatient().getPatientId() + ")", request);
            }
            String loginId = userDetails != null ? userDetails.getUsername() : form.getEmail();
            systemLogService.saveLog(com.example.dental.enums.LogActionType.APPOINTMENT_CREATE, loginId, clinic, "患者側から予約作成 (ID: " + appt.getAppointmentId() + ")", request);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }

        // セッションクリア
        session.removeAttribute("verificationCode");
        session.removeAttribute("verificationCodeExpiry");
        session.removeAttribute("verificationEmail");

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // --- API: 予約の確定（既存患者／メール変更なし） ---
    @PostMapping("/api/reserve-existing")
    @ResponseBody
    public ResponseEntity<?> confirmReservationExisting(@PathVariable String token,
                                                        @AuthenticationPrincipal PatientUserDetails userDetails,
                                                        @RequestBody @Validated({
                                                            PatientReservationForm.Step1.class,
                                                            PatientReservationForm.Step2.class,
                                                            PatientReservationForm.Step3.class
                                                        }) PatientReservationForm form,
                                                        BindingResult result,
                                                        jakarta.servlet.http.HttpServletRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "ログインが必要です。"));
        }

        DentalClinic clinic = getClinicOrThrow(token);

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", String.join("\n", errors)));
        }

        // 不正日時チェック・予約制限チェック
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        int maxMonths = clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3;
        LocalDate maxAllowedDate = today.plusMonths(maxMonths);

        if (form.getReservationDate().isBefore(today)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "過去の日は予約できません。"));
        }
        if (form.getReservationDate().isEqual(today) && form.getReservationTime().isBefore(LocalTime.now(java.time.ZoneId.of("Asia/Tokyo")))) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "過去の時間は予約できません。"));
        }
        if (form.getReservationDate().isAfter(maxAllowedDate)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "予約可能期間（" + maxMonths + "ヶ月先まで）を超えています。"));
        }
        if (clinic.getReservationRestrictions() != null && clinic.getReservationRestrictions() && form.getReservationDate().isEqual(today)) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "当日のご予約は受け付けておりません。"));
        }

        // 複合制限（同一医院内で同じ電話番号の二重登録を防ぐ）
        java.util.Optional<com.example.dental.entity.Patient> duplicatePatientOpt = patientRepository.findByDentalClinicAndTel(clinic, form.getTel());
        if (duplicatePatientOpt.isPresent() && !duplicatePatientOpt.get().getPatientId().equals(userDetails.getPatient().getPatientId())) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "この電話番号は既に別のアカウントで登録されています。"));
        }

        TreatmentType treatment = treatmentTypeRepository.findById(form.getTreatmentId()).orElseThrow();

        try {
            com.example.dental.entity.Appointment appt = patientReservationService.createAppointmentForExistingPatient(form, clinic, treatment, userDetails.getPatient());
            // 予約完了メール送信
            emailService.sendReservationCompleteEmail(form.getEmail(), appt);
            
            systemLogService.saveLog(com.example.dental.enums.LogActionType.APPOINTMENT_CREATE, userDetails.getUsername(), clinic, "患者側から予約作成 (ID: " + appt.getAppointmentId() + ")", request);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
