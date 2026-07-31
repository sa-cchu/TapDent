package com.example.dental.service;

import com.example.dental.dto.DentistDto;
import com.example.dental.entity.Dentist;
import com.example.dental.entity.DentalClinic;
import com.example.dental.form.DentistForm;
import com.example.dental.repository.DentistRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.entity.DentistTreatment;
import com.example.dental.entity.TreatmentType;
import com.example.dental.repository.DentistTreatmentRepository;
import com.example.dental.repository.TreatmentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DentistService {

    private final DentistRepository dentistRepository;
    private final DentalClinicRepository dentalClinicRepository;
    private final DentistTreatmentRepository dentistTreatmentRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;

    public DentistService(DentistRepository dentistRepository,
                          DentalClinicRepository dentalClinicRepository,
                          DentistTreatmentRepository dentistTreatmentRepository,
                          TreatmentTypeRepository treatmentTypeRepository) {
        this.dentistRepository = dentistRepository;
        this.dentalClinicRepository = dentalClinicRepository;
        this.dentistTreatmentRepository = dentistTreatmentRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<DentistDto> getDentistsByLoginId(String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        return dentistRepository.findByDentalClinic(clinic).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DentistDto getDentistDtoByIdAndLoginId(Long dentistId, String loginId) {
        Dentist dentist = getDentistByIdAndLoginId(dentistId, loginId);
        return convertToDto(dentist);
    }

    @Transactional(readOnly = true)
    private Dentist getDentistByIdAndLoginId(Long dentistId, String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid dentistId"));
        
        if (!dentist.getDentalClinic().getDentalId().equals(clinic.getDentalId())) {
            throw new IllegalArgumentException("Not authorized to access this dentist");
        }
        return dentist;
    }

    public void saveDentist(String loginId, DentistForm form) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));

        Dentist dentist = new Dentist();
        dentist.setDentalClinic(clinic);
        dentist.setDentistName(form.getDentistName());
        dentist.setStatus(form.getStatus());

        dentistRepository.save(dentist);
        
        saveDentistTreatments(dentist, form.getTreatmentIds());
    }

    public void updateDentist(Long dentistId, String loginId, DentistForm form) {
        Dentist dentist = getDentistByIdAndLoginId(dentistId, loginId);
        
        dentist.setDentistName(form.getDentistName());
        dentist.setStatus(form.getStatus());

        dentistRepository.save(dentist);
        
        dentistTreatmentRepository.deleteByDentist(dentist);
        dentistTreatmentRepository.flush();
        
        saveDentistTreatments(dentist, form.getTreatmentIds());
    }

    private void saveDentistTreatments(Dentist dentist, List<Long> treatmentIds) {
        if (treatmentIds == null || treatmentIds.isEmpty()) {
            return;
        }
        for (Long treatmentId : treatmentIds) {
            TreatmentType treatmentType = treatmentTypeRepository.findById(treatmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid treatmentId"));
            
            if (!treatmentType.getDentalClinic().getDentalId().equals(dentist.getDentalClinic().getDentalId())) {
                throw new IllegalArgumentException("Not authorized to access this treatment");
            }
            
            DentistTreatment dt = new DentistTreatment();
            dt.setDentist(dentist);
            dt.setTreatmentType(treatmentType);
            dentistTreatmentRepository.save(dt);
        }
    }

    public void deleteDentist(Long dentistId, String loginId) {
        Dentist dentist = getDentistByIdAndLoginId(dentistId, loginId);
        dentistRepository.delete(dentist);
    }

    private DentistDto convertToDto(Dentist dentist) {
        DentistDto dto = new DentistDto();
        dto.setDentistId(dentist.getDentistId());
        if (dentist.getDentalClinic() != null) {
            dto.setDentalId(dentist.getDentalClinic().getDentalId());
        }
        dto.setDentistName(dentist.getDentistName());
        dto.setStatus(dentist.getStatus());
        
        List<Long> treatmentIds = dentistTreatmentRepository.findByDentist(dentist).stream()
                .map(dt -> dt.getTreatmentType().getTreatmentId())
                .collect(Collectors.toList());
        dto.setTreatmentIds(treatmentIds);
        
        return dto;
    }
}
