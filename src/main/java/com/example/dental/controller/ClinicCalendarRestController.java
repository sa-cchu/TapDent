package com.example.dental.controller;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.dental.entity.Appointment;
import com.example.dental.entity.DentalChair;
import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.DentalChairRepository;
import com.example.dental.repository.DentalClinicRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clinic/api/reservations")
public class ClinicCalendarRestController {

    private final AppointmentRepository appointmentRepository;
    private final DentalChairRepository dentalChairRepository;
    private final DentalClinicRepository dentalClinicRepository;
    private final com.example.dental.service.DentalClinicService dentalClinicService;

    private final com.example.dental.repository.TreatmentTypeRepository treatmentTypeRepository;
    private final com.example.dental.repository.DentistRepository dentistRepository;
    private final com.example.dental.repository.PatientRepository patientRepository;
    private final com.example.dental.service.ClinicReservationService clinicReservationService;
    private final com.example.dental.service.AppointmentService appointmentService;
    private final com.example.dental.service.SystemLogService systemLogService;

    public ClinicCalendarRestController(AppointmentRepository appointmentRepository,
                                        DentalChairRepository dentalChairRepository,
                                        DentalClinicRepository dentalClinicRepository,
                                        com.example.dental.service.DentalClinicService dentalClinicService,
                                        com.example.dental.repository.TreatmentTypeRepository treatmentTypeRepository,
                                        com.example.dental.repository.DentistRepository dentistRepository,
                                        com.example.dental.repository.PatientRepository patientRepository,
                                        com.example.dental.service.ClinicReservationService clinicReservationService,
                                        com.example.dental.service.AppointmentService appointmentService,
                                        com.example.dental.service.SystemLogService systemLogService) {
        this.appointmentRepository = appointmentRepository;
        this.dentalChairRepository = dentalChairRepository;
        this.dentalClinicRepository = dentalClinicRepository;
        this.dentalClinicService = dentalClinicService;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.dentistRepository = dentistRepository;
        this.patientRepository = patientRepository;
        this.clinicReservationService = clinicReservationService;
        this.appointmentService = appointmentService;
        this.systemLogService = systemLogService;
    }

    private DentalClinic getClinic(UserDetails userDetails) {
        return dentalClinicRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("クリニックが見つかりません"));
    }

    @GetMapping("/business-hours")
    public ResponseEntity<?> getBusinessHours(@AuthenticationPrincipal UserDetails userDetails) {
        DentalClinic clinic = getClinic(userDetails);
        return ResponseEntity.ok(dentalClinicService.getBusinessHours(clinic.getDentalId()));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        DentalClinic clinic = getClinic(userDetails);
        
        // start日の00:00:00から、end日の23:59:59までを取得
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        
        List<Appointment> appointments = appointmentRepository.findByDentalClinicAndStartAtBetween(clinic, startDateTime, endDateTime);
        
        List<Map<String, Object>> response = appointments.stream()
            .filter(a -> a.getStatus() != com.example.dental.enums.AppointmentStatus.CANCELLED)
            .map(a -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("appointmentId", a.getAppointmentId());
            if (a.getPatient() != null) {
                map.put("patientId", a.getPatient().getPatientId());
                map.put("patientName", a.getPatient().getName());
                map.put("patientNameKana", a.getPatient().getPronunciationGuide());
                map.put("patientCode", a.getPatient().getPatientCode());
                map.put("patientTel", a.getPatient().getTel());
            } else if (a.getToken() != null) {
                map.put("patientName", a.getToken().getName());
                map.put("patientNameKana", a.getToken().getNameKana() != null ? a.getToken().getNameKana() : "");
                map.put("patientCode", "ゲスト");
                map.put("patientTel", a.getToken().getTell());
            }
            map.put("startAt", a.getStartAt() != null ? a.getStartAt().toString() : null);
            map.put("endAt", a.getEndAt() != null ? a.getEndAt().toString() : null);
            map.put("status", a.getStatus().name());
            map.put("visitType", a.getVisitType().name());
            map.put("treatmentId", a.getTreatmentType().getTreatmentId());
            map.put("treatmentName", a.getTreatmentType().getTreatmentName());
            map.put("appointMethod", a.getAppointMethod().name());
            map.put("chairId", a.getDentalChair().getChairId());
            map.put("chairName", a.getDentalChair().getChairName());
            map.put("dentistId", a.getDentist().getDentistId());
            map.put("dentistName", a.getDentist().getDentistName());
            map.put("patientComment", a.getPatientComment() != null ? a.getPatientComment() : "");
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chairs")
    public ResponseEntity<?> getChairs(@AuthenticationPrincipal UserDetails userDetails) {
        DentalClinic clinic = getClinic(userDetails);
        List<DentalChair> chairs = dentalChairRepository.findByDentalClinicAndIsDeletedFalse(clinic);
        
        List<Map<String, Object>> response = chairs.stream().map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("chairId", c.getChairId());
            map.put("chairName", c.getChairName());
            map.put("status", c.getStatus());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/form-data")
    public ResponseEntity<?> getFormData(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestParam(required = false) String targetPatientType) {
        DentalClinic clinic = getClinic(userDetails);
        
        // Treatments
        List<com.example.dental.entity.TreatmentType> treatments = treatmentTypeRepository.findByDentalClinicAndIsDeletedFalse(clinic);
        if (targetPatientType != null && !targetPatientType.isEmpty()) {
            treatments = treatments.stream()
                .filter(t -> t.getTargetPatientType().name().equals("BOTH") || t.getTargetPatientType().name().equals(targetPatientType))
                .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> treatmentList = treatments.stream()
            .filter(t -> Boolean.TRUE.equals(t.getStatus())) // 公開中のみ
            .map(t -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("treatmentId", t.getTreatmentId());
                map.put("treatmentName", t.getTreatmentName());
                map.put("requiredMinutes", t.getRequiredMinutes());
                map.put("targetPatientType", t.getTargetPatientType().name());
                return map;
            }).collect(Collectors.toList());
            
        // Dentists
        List<com.example.dental.entity.Dentist> dentists = dentistRepository.findByDentalClinicAndIsDeletedFalse(clinic);
        List<Map<String, Object>> dentistList = dentists.stream()
            .filter(d -> Boolean.TRUE.equals(d.getStatus()))
            .map(d -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("dentistId", d.getDentistId());
                map.put("dentistName", d.getDentistName());
                return map;
            }).collect(Collectors.toList());
            
        return ResponseEntity.ok(Map.of(
            "treatments", treatmentList,
            "dentists", dentistList
        ));
    }

    @GetMapping("/search-patients")
    public ResponseEntity<?> searchPatients(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam String q) {
        DentalClinic clinic = getClinic(userDetails);
        
        List<com.example.dental.entity.Patient> patients = patientRepository.searchPatients(clinic.getDentalId(), q);
            
        List<Map<String, Object>> response = patients.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("patientId", p.getPatientId());
            map.put("patientCode", p.getPatientCode());
            map.put("name", p.getName());
            map.put("kana", p.getPronunciationGuide());
            map.put("tel", p.getTel());
            
            String genderStr = "不明";
            if ("1".equals(p.getGender()) || "男".equals(p.getGender()) || "男性".equals(p.getGender())) genderStr = "男性";
            else if ("2".equals(p.getGender()) || "女".equals(p.getGender()) || "女性".equals(p.getGender())) genderStr = "女性";
            else if ("3".equals(p.getGender())) genderStr = "その他";
            map.put("gender", genderStr);
            
            map.put("birthday", p.getBirthday() != null ? p.getBirthday().toString() : "不明");
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-appointments")
    public ResponseEntity<?> searchAppointments(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam String q) {
        DentalClinic clinic = getClinic(userDetails);
        
        // Search using the new repository method (limit to top 50 via Pageable)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 50);
        List<Appointment> appointments = appointmentRepository.searchAppointmentsByKeyword(clinic.getDentalId(), q, pageable);
            
        List<Map<String, Object>> response = appointments.stream()
            .map(a -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("appointmentId", a.getAppointmentId());
                if (a.getPatient() != null) {
                    map.put("patientId", a.getPatient().getPatientId());
                    map.put("patientName", a.getPatient().getName());
                    map.put("patientNameKana", a.getPatient().getPronunciationGuide());
                    map.put("patientCode", a.getPatient().getPatientCode());
                    map.put("patientTel", a.getPatient().getTel());
                } else if (a.getToken() != null) {
                    map.put("patientName", a.getToken().getName());
                    map.put("patientNameKana", a.getToken().getNameKana() != null ? a.getToken().getNameKana() : "");
                    map.put("patientCode", "ゲスト");
                    map.put("patientTel", a.getToken().getTell());
                }
                map.put("startAt", a.getStartAt() != null ? a.getStartAt().toString() : null);
                map.put("endAt", a.getEndAt() != null ? a.getEndAt().toString() : null);
                map.put("status", a.getStatus().name());
                map.put("visitType", a.getVisitType().name());
                map.put("treatmentId", a.getTreatmentType().getTreatmentId());
                map.put("treatmentName", a.getTreatmentType().getTreatmentName());
                map.put("appointMethod", a.getAppointMethod().name());
                map.put("chairId", a.getDentalChair().getChairId());
                map.put("chairName", a.getDentalChair().getChairName());
                map.put("dentistId", a.getDentist().getDentistId());
                map.put("dentistName", a.getDentist().getDentistName());
                map.put("patientComment", a.getPatientComment() != null ? a.getPatientComment() : "");
                return map;
            }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-dates")
    public ResponseEntity<?> getAvailableDates(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam int year,
                                               @RequestParam int month,
                                               @RequestParam Long treatmentId,
                                               @RequestParam(required = false) Long dentistId,
                                               @RequestParam(required = false) Long chairId,
                                               @RequestParam(required = false) Long excludeApptId) {
        DentalClinic clinic = getClinic(userDetails);
        com.example.dental.entity.TreatmentType treatment = treatmentTypeRepository.findById(treatmentId).orElseThrow();
        
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        LocalDate today = LocalDate.now();

        Map<String, String> statusMap = new java.util.HashMap<>();
        
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.isBefore(today)) {
                statusMap.put(d.toString(), "-");
                continue;
            }
            if (appointmentService.isClinicHoliday(clinic, d)) {
                statusMap.put(d.toString(), "-");
                continue;
            }
            
            List<LocalTime> slots = appointmentService.getAvailableTimeSlots(clinic, d, treatment, dentistId, chairId, excludeApptId);
            if (d.isEqual(today)) {
                LocalTime now = LocalTime.now();
                slots = slots.stream().filter(t -> t.isAfter(now)).collect(Collectors.toList());
            }
            
            if (slots.isEmpty()) {
                statusMap.put(d.toString(), "×");
            } else {
                statusMap.put(d.toString(), "○");
            }
        }
        
        return ResponseEntity.ok(statusMap);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlots(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                               @RequestParam Long treatmentId,
                                               @RequestParam(required = false) Long dentistId,
                                               @RequestParam(required = false) Long chairId,
                                               @RequestParam(required = false) Long excludeApptId) {
        DentalClinic clinic = getClinic(userDetails);
        com.example.dental.entity.TreatmentType treatment = treatmentTypeRepository.findById(treatmentId).orElseThrow();
        
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return ResponseEntity.ok(List.of());
        }

        List<LocalTime> slots = appointmentService.getAvailableTimeSlots(clinic, date, treatment, dentistId, chairId, excludeApptId);
        
        if (date.isEqual(today)) {
            LocalTime now = LocalTime.now();
            slots = slots.stream().filter(time -> time.isAfter(now)).collect(Collectors.toList());
        }

        return ResponseEntity.ok(slots.stream().map(LocalTime::toString).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody com.example.dental.dto.ReservationFormDto dto,
                                               jakarta.servlet.http.HttpServletRequest request) {
        try {
            DentalClinic clinic = getClinic(userDetails);
            com.example.dental.entity.Appointment appointment = clinicReservationService.createClinicReservation(clinic, dto);
            
            systemLogService.saveLog(com.example.dental.enums.LogActionType.APPOINTMENT_CREATE, userDetails.getUsername(), clinic, "医院側から予約を作成 (ID: " + appointment.getAppointmentId() + ")", request);
            
            return ResponseEntity.ok(Map.of("success", true, "appointmentId", appointment.getAppointmentId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<?> updateReservationStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long appointmentId,
                                                     @RequestBody Map<String, String> requestBody,
                                                     jakarta.servlet.http.HttpServletRequest request) {
        try {
            DentalClinic clinic = getClinic(userDetails);
            String statusStr = requestBody.get("status");
            com.example.dental.enums.AppointmentStatus status = com.example.dental.enums.AppointmentStatus.valueOf(statusStr);
            appointmentService.updateAppointmentStatus(appointmentId, status, clinic);
            
            com.example.dental.enums.LogActionType logType = status == com.example.dental.enums.AppointmentStatus.CANCELLED ? 
                com.example.dental.enums.LogActionType.APPOINTMENT_CANCEL : com.example.dental.enums.LogActionType.APPOINTMENT_UPDATE;
            systemLogService.saveLog(logType, userDetails.getUsername(), clinic, "予約のステータス変更 (ID: " + appointmentId + ", 新ステータス: " + status.name() + ")", request);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "無効なステータスまたは予約です"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<?> updateReservationFull(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Long appointmentId,
                                                   @RequestBody com.example.dental.dto.ReservationFormDto dto,
                                                   jakarta.servlet.http.HttpServletRequest request) {
        try {
            DentalClinic clinic = getClinic(userDetails);
            com.example.dental.entity.TreatmentType treatment = treatmentTypeRepository.findById(dto.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("診療メニューが見つかりません"));
                
            com.example.dental.entity.Appointment appointment = appointmentService.updateAppointmentFull(
                appointmentId, clinic, treatment, dto.getReservationDate(), dto.getReservationTime(), 
                dto.getDentistId(), dto.getChairId(), dto.getAppointMethod(), dto.getPatientComment());
                
            systemLogService.saveLog(com.example.dental.enums.LogActionType.APPOINTMENT_UPDATE, userDetails.getUsername(), clinic, "予約内容の変更 (ID: " + appointment.getAppointmentId() + ")", request);

            return ResponseEntity.ok(Map.of("success", true, "appointmentId", appointment.getAppointmentId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "予期せぬエラーが発生しました: " + e.getMessage()));
        }
    }
}
