package com.example.dental.service;

import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.TreatmentType;
import com.example.dental.form.TreatmentTypeForm;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.TreatmentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TreatmentTypeService {

    private final TreatmentTypeRepository treatmentTypeRepository;
    private final DentalClinicRepository dentalClinicRepository;

    public TreatmentTypeService(TreatmentTypeRepository treatmentTypeRepository, DentalClinicRepository dentalClinicRepository) {
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.dentalClinicRepository = dentalClinicRepository;
    }

    @Transactional(readOnly = true)
    public List<TreatmentType> getTreatmentsByLoginId(String loginId, com.example.dental.enums.TargetPatientType targetPatientType) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        if (targetPatientType != null) {
            return treatmentTypeRepository.findByDentalClinicAndTargetPatientType(clinic, targetPatientType);
        }
        return treatmentTypeRepository.findByDentalClinic(clinic);
    }

    @Transactional(readOnly = true)
    public TreatmentType getTreatmentByIdAndLoginId(Long treatmentId, String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        TreatmentType treatment = treatmentTypeRepository.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatmentId"));
        
        if (!treatment.getDentalClinic().getDentalId().equals(clinic.getDentalId())) {
            throw new IllegalArgumentException("Not authorized to access this treatment");
        }
        return treatment;
    }

    public void saveTreatment(String loginId, TreatmentTypeForm form) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));

        TreatmentType treatment = new TreatmentType();
        treatment.setDentalClinic(clinic);
        treatment.setTreatmentName(form.getTreatmentName());
        treatment.setRequiredMinutes(form.getRequiredMinutes());
        treatment.setStatus(form.getStatus());
        treatment.setTargetPatientType(form.getTargetPatientType());
        treatment.setTargetStaffType(form.getTargetStaffType());

        treatmentTypeRepository.save(treatment);
    }

    public void updateTreatment(Long treatmentId, String loginId, TreatmentTypeForm form) {
        TreatmentType treatment = getTreatmentByIdAndLoginId(treatmentId, loginId);
        
        treatment.setTreatmentName(form.getTreatmentName());
        treatment.setRequiredMinutes(form.getRequiredMinutes());
        treatment.setStatus(form.getStatus());
        treatment.setTargetPatientType(form.getTargetPatientType());
        treatment.setTargetStaffType(form.getTargetStaffType());

        treatmentTypeRepository.save(treatment);
    }

    public void deleteTreatment(Long treatmentId, String loginId) {
        TreatmentType treatment = getTreatmentByIdAndLoginId(treatmentId, loginId);
        treatmentTypeRepository.delete(treatment);
    }
}
