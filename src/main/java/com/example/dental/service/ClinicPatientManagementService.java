package com.example.dental.service;

import com.example.dental.dto.AppointmentDto;
import com.example.dental.dto.PatientDetailDto;
import com.example.dental.dto.PatientDto;
import com.example.dental.entity.Appointment;
import com.example.dental.entity.Patient;
import com.example.dental.form.ClinicPatientEditForm;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicPatientManagementService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DentalClinicRepository dentalClinicRepository;

    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(Long dentalId, String keyword) {
        List<Patient> patients = patientRepository.searchPatients(dentalId, keyword);
        return patients.stream().map(this::toPatientDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDetailDto getPatientDetail(Long dentalId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));
        if (!patient.getDentalClinic().getDentalId().equals(dentalId)) {
            throw new IllegalArgumentException("権限がありません");
        }

        PatientDetailDto detailDto = new PatientDetailDto();
        detailDto.setPatient(toPatientDto(patient));

        List<Appointment> appointments = appointmentRepository.findByPatientPatientIdOrderByStartAtDesc(patientId);
        List<AppointmentDto> appointmentDtos = appointments.stream()
                .map(this::toAppointmentDto)
                .collect(Collectors.toList());
        detailDto.setAppointments(appointmentDtos);

        return detailDto;
    }

    @Transactional
    public void updatePatient(Long dentalId, Long patientId, ClinicPatientEditForm form) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));
        if (!patient.getDentalClinic().getDentalId().equals(dentalId)) {
            throw new IllegalArgumentException("権限がありません");
        }

        // 電話番号の重複チェック
        if (!patient.getTel().equals(form.getTel())) {
            Optional<Patient> existingTel = patientRepository.findByDentalClinicAndTel(patient.getDentalClinic(),
                    form.getTel());
            if (existingTel.isPresent() && !existingTel.get().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException("この電話番号は既に別のアカウントで登録されています。");
            }
        }

        // 診察券番号の重複チェック
        if (!form.getPatientCode().equals(patient.getPatientCode())) {
            Optional<Patient> existingCode = patientRepository
                    .findByDentalClinicDentalIdAndPatientCodeAndIsDeletedFalse(dentalId, form.getPatientCode());
            if (existingCode.isPresent() && !existingCode.get().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException("この診察券番号は既に登録されています。");
            }
        }

        patient.setPatientCode(form.getPatientCode());
        patient.setName(form.getName());
        patient.setPronunciationGuide(form.getPronunciationGuide());
        patient.setBirthday(form.getBirthday());
        patient.setGender(form.getGender());
        patient.setTel(form.getTel());
        patient.setStatus(form.getStatus());

        patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long dentalId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));
        if (!patient.getDentalClinic().getDentalId().equals(dentalId)) {
            throw new IllegalArgumentException("権限がありません");
        }

        patient.setIsDeleted(true);
        patientRepository.save(patient);
    }

    private PatientDto toPatientDto(Patient p) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(p.getPatientId());
        dto.setDentalId(p.getDentalClinic().getDentalId());
        dto.setPatientCode(p.getPatientCode());
        dto.setName(p.getName());
        dto.setPronunciationGuide(p.getPronunciationGuide());
        dto.setGender(p.getGender());
        dto.setBirthday(p.getBirthday());
        dto.setTel(p.getTel());
        dto.setEmail(p.getEmail());
        dto.setStatus(p.getStatus());
        dto.setRoleId(p.getRole() != null ? p.getRole().getRoleId() : null);
        dto.setLoginAttempts(p.getLoginAttempts());
        dto.setLockedUntill(p.getLockedUntill());
        dto.setDeleteFrag(p.getIsDeleted());
        return dto;
    }

    private AppointmentDto toAppointmentDto(Appointment a) {
        AppointmentDto dto = new AppointmentDto();
        dto.setAppointmentId(a.getAppointmentId());
        dto.setDentalId(a.getDentalClinic().getDentalId());
        if (a.getPatient() != null) {
            dto.setPatientId(a.getPatient().getPatientId());
        }
        if (a.getToken() != null) {
            dto.setTokenId(a.getToken().getTokenId());
        }
        if (a.getDentalChair() != null) {
            dto.setChairId(a.getDentalChair().getChairId());
        }
        if (a.getDentist() != null) {
            dto.setDentistId(a.getDentist().getDentistId());
        }
        if (a.getTreatmentType() != null) {
            dto.setTreatmentId(a.getTreatmentType().getTreatmentId());
        }
        dto.setAppointMethod(a.getAppointMethod());
        dto.setStartAt(a.getStartAt());
        dto.setEndAt(a.getEndAt());
        dto.setPatientComment(a.getPatientComment());
        dto.setStatus(a.getStatus());
        dto.setVisitType(a.getVisitType());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
