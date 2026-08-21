package com.example.dental.service;

import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Patient;
import com.example.dental.entity.Role;
import com.example.dental.enums.PatientStatus;
import com.example.dental.enums.RoleName;
import com.example.dental.form.PatientReservationForm;
import com.example.dental.repository.PatientRepository;
import com.example.dental.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.dental.entity.Appointment;
import com.example.dental.enums.VisitType;

@Service
public class PatientReservationService {

    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentService appointmentService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public PatientReservationService(PatientRepository patientRepository,
                                     RoleRepository roleRepository,
                                     PasswordEncoder passwordEncoder,
                                     AppointmentService appointmentService,
                                     org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentService = appointmentService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void fixDatabaseSchema() {
        try {
            jdbcTemplate.execute("ALTER TABLE patients MODIFY patient_code VARCHAR(20) NULL;");
        } catch (Exception e) {
            // エラーは無視（すでにNULL許可されているか、権限がない場合など）
        }
        try {
            jdbcTemplate.execute("ALTER TABLE patients MODIFY locked_untill DATETIME(6) NULL;");
            // すでに過去の日付が入っているデータをNULLに更新
            jdbcTemplate.execute("UPDATE patients SET locked_untill = NULL WHERE locked_untill < NOW();");
        } catch (Exception e) {
            // エラーは無視
        }
        try {
            jdbcTemplate.execute("ALTER TABLE patients DROP COLUMN delete_frag;");
        } catch (Exception e) {
            // 無視
        }
        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP COLUMN update_at;");
        } catch (Exception e) {
            // 無視
        }
    }

    /**
     * 新規患者アカウントを作成し、そのまま予約を確定します
     */
    @Transactional
    public Appointment registerPatientAndAppointment(PatientReservationForm form, DentalClinic clinic, com.example.dental.entity.TreatmentType treatment) {
        // 1. 新規患者登録
        Patient patient = new Patient();
        patient.setDentalClinic(clinic);
        patient.setName(form.getName());
        patient.setPronunciationGuide(form.getPronunciationGuide());
        patient.setBirthday(form.getBirthday());
        patient.setGender(form.getGender());
        patient.setTel(form.getTel());
        patient.setEmail(form.getEmail());
        
        // 生年月日から初期パスワード（YYYYMMDD）を生成
        String initialPassword = form.getBirthday().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        patient.setPassword(passwordEncoder.encode(initialPassword));
        
        patient.setStatus(PatientStatus.ACTIVE);
        
        // 役割の取得・設定
        Role role = roleRepository.findByRoleName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new IllegalStateException("ROLE_PATIENT が見つかりません"));
        patient.setRole(role);
        
        patient.setLoginAttempts(0);
        patient.setLockedUntill(null); // ロックされていない状態はNULL
        patient.setIsDeleted(false);

        // ※ 診察券番号 (patientCode) は来院時に発行するため初回登録時は null とする想定
        patient = patientRepository.save(patient);

        // 2. 予約の作成
        return appointmentService.createAppointment(clinic, patient, treatment, form.getReservationDate(), form.getReservationTime(), form.getPatientComment(), VisitType.FIRST_VISIT);
    }

    /**
     * 既存患者の予約を確定します
     * （必要に応じて患者情報も更新します）
     */
    @Transactional
    public Appointment createAppointmentForExistingPatient(PatientReservationForm form, DentalClinic clinic, com.example.dental.entity.TreatmentType treatment, Patient existingPatient) {
        // 患者情報の更新（必要であれば）
        // 氏名や電話番号などは変更不可とするか、フォームの入力値で上書きするかは要件次第ですが
        // 今回はとりあえず、フォームから送られてきた内容で上書き（更新）します
        existingPatient.setName(form.getName());
        existingPatient.setPronunciationGuide(form.getPronunciationGuide());
        existingPatient.setBirthday(form.getBirthday());
        existingPatient.setGender(form.getGender());
        existingPatient.setTel(form.getTel());
        existingPatient.setEmail(form.getEmail());
        
        if (form.getPatientCode() != null && !form.getPatientCode().isBlank()) {
            existingPatient.setPatientCode(form.getPatientCode());
        }
        
        patientRepository.save(existingPatient);

        // 予約の作成
        return appointmentService.createAppointment(clinic, existingPatient, treatment, form.getReservationDate(), form.getReservationTime(), form.getPatientComment(), VisitType.RE_VISIT);
    }
}
