package com.example.dental.service;

import com.example.dental.entity.Patient;
import com.example.dental.form.PatientAccountForm;
import com.example.dental.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 患者の基本情報を更新します。
     * メールアドレスが変更された場合、他患者との重複チェック及びパスワードの検証を行います。
     * 
     * @param patientId 患者ID
     * @param form 入力フォーム
     * @throws IllegalArgumentException 検証エラー時
     */
    @Transactional
    public void updatePatientInfo(Long patientId, PatientAccountForm form) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));

        boolean isEmailChanged = !patient.getEmail().equals(form.getEmail());

        if (isEmailChanged) {
            // メールアドレスの重複チェック（同じ医院内で）
            Optional<Patient> existingPatient = patientRepository.findByDentalClinicAndEmail(patient.getDentalClinic(), form.getEmail());
            if (existingPatient.isPresent() && !existingPatient.get().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException("入力されたメールアドレスは既に登録されています。");
            }
        }

        boolean isTelChanged = !patient.getTel().equals(form.getTel());
        if (isTelChanged) {
            // 複合制限（同一医院内で同じ電話番号の二重登録を防ぐ）
            Optional<Patient> duplicatePatientOpt = patientRepository.findByDentalClinicAndTel(patient.getDentalClinic(), form.getTel());
            if (duplicatePatientOpt.isPresent() && !duplicatePatientOpt.get().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException("この電話番号は既に別のアカウントで登録されています。");
            }
        }

        patient.setName(form.getName());
        patient.setPronunciationGuide(form.getPronunciationGuide());
        patient.setBirthday(form.getBirthday());
        patient.setGender(form.getGender());
        patient.setTel(form.getTel());
        patient.setEmail(form.getEmail());

        patientRepository.save(patient);
    }

    /**
     * 患者のパスワードを更新します。
     *
     * @param patientId 患者ID
     * @param rawNewPassword 新しいパスワード(平文)
     */
    @Transactional
    public void updatePatientPassword(Long patientId, String rawNewPassword) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));
        
        patient.setPassword(passwordEncoder.encode(rawNewPassword));
        patientRepository.save(patient);
    }
}
