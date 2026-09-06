package com.example.dental.controller;
import java.time.format.DateTimeFormatter;

import com.example.dental.entity.Appointment;
import com.example.dental.entity.Patient;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.PatientRepository;
import com.example.dental.service.AppointmentService;
import com.example.dental.service.EmailService;
import com.example.dental.service.PatientUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/patient/{token}/reservation/{appointmentId}")
public class PatientReservationChangeController {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final EmailService emailService;

    public PatientReservationChangeController(PatientRepository patientRepository,
                                              AppointmentRepository appointmentRepository,
                                              AppointmentService appointmentService,
                                              EmailService emailService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
    }

    /**
     * 共通バリデーション：患者と予約の整合性をチェック
     */
    private Appointment validateAndGetAppointment(PatientUserDetails userDetails, Long appointmentId) {
        if (userDetails == null) {
            throw new IllegalArgumentException("ログインが必要です");
        }
        Patient patient = userDetails.getPatient();
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("予約が見つかりません"));
                
        if (appointment.getPatient() == null || !appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new IllegalArgumentException("他の患者の予約にはアクセスできません");
        }

        if (appointment.getStatus() != AppointmentStatus.RESERVED) {
            throw new IllegalStateException("この予約はすでにキャンセルまたは受診済みです");
        }
        
        if (appointment.getStartAt().toLocalDate().isEqual(LocalDate.now())) {
            throw new IllegalStateException("当日の変更・キャンセルはWebからは行えません");
        }

        return appointment;
    }

    /**
     * 変更・キャンセル選択画面
     */
    @GetMapping("/action")
    public String showActionChoice(@PathVariable String token, 
                                   @PathVariable Long appointmentId, 
                                   @AuthenticationPrincipal PatientUserDetails userDetails,
                                   Model model) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            model.addAttribute("token", token);
            model.addAttribute("appointment", appointment);
            return "patient/reservation_action";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }

    /**
     * 予約キャンセル確認画面
     */
    @GetMapping("/cancel")
    public String showCancelConfirm(@PathVariable String token, 
                                    @PathVariable Long appointmentId, 
                                    @AuthenticationPrincipal PatientUserDetails userDetails,
                                    Model model) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            model.addAttribute("token", token);
            model.addAttribute("appointment", appointment);
            return "patient/reservation_cancel_confirm";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }

    /**
     * 予約キャンセル実行
     */
    @PostMapping("/cancel")
    public String executeCancel(@PathVariable String token, 
                                @PathVariable Long appointmentId, 
                                @AuthenticationPrincipal PatientUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            appointmentService.cancelAppointment(appointmentId);
            
            // キャンセルメール送信
            Patient patient = appointment.getPatient();
            if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                emailService.sendReservationCancelEmail(patient.getEmail(), appointment);
            }

            redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
            return "redirect:/patient/" + token + "/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }

    /**
     * 予約時間変更画面
     */
    @GetMapping("/change")
    public String showChangeTime(@PathVariable String token, 
                                 @PathVariable Long appointmentId, 
                                 @AuthenticationPrincipal PatientUserDetails userDetails,
                                 Model model) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            model.addAttribute("token", token);
            model.addAttribute("appointment", appointment);
            model.addAttribute("treatmentId", appointment.getTreatmentType().getTreatmentId());
            model.addAttribute("clinicName", appointment.getDentalClinic().getName());
            return "patient/reservation_change";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }

    /**
     * 予約時間変更の確認画面
     */
    @PostMapping("/change-confirm")
    public String showChangeConfirm(@PathVariable String token, 
                                    @PathVariable Long appointmentId, 
                                    @AuthenticationPrincipal PatientUserDetails userDetails,
                                    @RequestParam String reservationDate,
                                    @RequestParam String reservationTime,
                                    Model model) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            
            LocalDate newDate = LocalDate.parse(reservationDate);
            LocalTime newTime = LocalTime.parse(reservationTime);
            LocalDateTime newStartAt = LocalDateTime.of(newDate, newTime);
            
            LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
            int maxMonths = appointment.getDentalClinic().getMaxReserveMonth() != null ? appointment.getDentalClinic().getMaxReserveMonth() : 3;
            LocalDate maxAllowedDate = today.plusMonths(maxMonths);

            if (newDate.isBefore(today)) {
                throw new IllegalStateException("過去の日は予約できません。");
            }
            if (newDate.isEqual(today) && newTime.isBefore(LocalTime.now(java.time.ZoneId.of("Asia/Tokyo")))) {
                throw new IllegalStateException("過去の時間は予約できません。");
            }
            if (newDate.isAfter(maxAllowedDate)) {
                throw new IllegalStateException("予約可能期間（" + maxMonths + "ヶ月先まで）を超えています。");
            }
            if (appointment.getDentalClinic().getReservationRestrictions() != null && appointment.getDentalClinic().getReservationRestrictions() && newDate.isEqual(today)) {
                throw new IllegalStateException("当日のご予約は受け付けておりません。");
            }

            model.addAttribute("token", token);
            model.addAttribute("appointment", appointment);
            model.addAttribute("reservationDate", reservationDate);
            model.addAttribute("reservationTime", reservationTime);
            model.addAttribute("newStartAt", newStartAt);
            
            return "patient/reservation_change_confirm";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }

    /**
     * 予約時間変更実行
     */
    @PostMapping("/change")
    public String executeChangeTime(@PathVariable String token, 
                                    @PathVariable Long appointmentId, 
                                    @AuthenticationPrincipal PatientUserDetails userDetails,
                                    @RequestParam String reservationDate,
                                    @RequestParam String reservationTime,
                                    RedirectAttributes redirectAttributes) {
        try {
            Appointment appointment = validateAndGetAppointment(userDetails, appointmentId);
            
            LocalDate newDate = LocalDate.parse(reservationDate);
            LocalTime newTime = LocalTime.parse(reservationTime);
            
            LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
            int maxMonths = appointment.getDentalClinic().getMaxReserveMonth() != null ? appointment.getDentalClinic().getMaxReserveMonth() : 3;
            LocalDate maxAllowedDate = today.plusMonths(maxMonths);

            if (newDate.isBefore(today)) {
                throw new IllegalStateException("過去の日は予約できません。");
            }
            if (newDate.isEqual(today) && newTime.isBefore(LocalTime.now(java.time.ZoneId.of("Asia/Tokyo")))) {
                throw new IllegalStateException("過去の時間は予約できません。");
            }
            if (newDate.isAfter(maxAllowedDate)) {
                throw new IllegalStateException("予約可能期間（" + maxMonths + "ヶ月先まで）を超えています。");
            }
            if (appointment.getDentalClinic().getReservationRestrictions() != null && appointment.getDentalClinic().getReservationRestrictions() && newDate.isEqual(today)) {
                throw new IllegalStateException("当日のご予約は受け付けておりません。");
            }

            Appointment updatedAppointment = appointmentService.changeAppointmentTime(appointmentId, newDate, newTime);
            
            // 変更メール送信
            Patient patient = updatedAppointment.getPatient();
            if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                emailService.sendReservationChangeEmail(patient.getEmail(), updatedAppointment);
            }

            redirectAttributes.addFlashAttribute("successMessage", "予約の日時を変更しました。");
            return "redirect:/patient/" + token + "/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/" + token + "/dashboard";
        }
    }
}
