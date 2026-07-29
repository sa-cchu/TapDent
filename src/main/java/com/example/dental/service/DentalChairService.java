package com.example.dental.service;

import com.example.dental.dto.DentalChairDto;
import com.example.dental.entity.DentalChair;
import com.example.dental.entity.DentalClinic;
import com.example.dental.form.DentalChairForm;
import com.example.dental.repository.DentalChairRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.entity.ChairTreatment;
import com.example.dental.entity.TreatmentType;
import com.example.dental.repository.ChairTreatmentRepository;
import com.example.dental.repository.TreatmentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DentalChairService {

    private final DentalChairRepository dentalChairRepository;
    private final DentalClinicRepository dentalClinicRepository;
    private final ChairTreatmentRepository chairTreatmentRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;

    public DentalChairService(DentalChairRepository dentalChairRepository, 
                              DentalClinicRepository dentalClinicRepository,
                              ChairTreatmentRepository chairTreatmentRepository,
                              TreatmentTypeRepository treatmentTypeRepository) {
        this.dentalChairRepository = dentalChairRepository;
        this.dentalClinicRepository = dentalClinicRepository;
        this.chairTreatmentRepository = chairTreatmentRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<DentalChairDto> getChairsByLoginId(String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        return dentalChairRepository.findByDentalClinic(clinic).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DentalChairDto getChairDtoByIdAndLoginId(Long chairId, String loginId) {
        DentalChair chair = getChairByIdAndLoginId(chairId, loginId);
        return convertToDto(chair);
    }

    @Transactional(readOnly = true)
    private DentalChair getChairByIdAndLoginId(Long chairId, String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));
        DentalChair chair = dentalChairRepository.findById(chairId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chairId"));
        
        if (!chair.getDentalClinic().getDentalId().equals(clinic.getDentalId())) {
            throw new IllegalArgumentException("Not authorized to access this chair");
        }
        return chair;
    }

    public void saveChair(String loginId, DentalChairForm form) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId"));

        DentalChair chair = new DentalChair();
        chair.setDentalClinic(clinic);
        chair.setChairName(form.getChairName());
        chair.setStatus(form.getStatus());

        dentalChairRepository.save(chair);
        
        saveChairTreatments(chair, form.getTreatmentIds());
    }

    public void updateChair(Long chairId, String loginId, DentalChairForm form) {
        DentalChair chair = getChairByIdAndLoginId(chairId, loginId);
        
        chair.setChairName(form.getChairName());
        chair.setStatus(form.getStatus());

        dentalChairRepository.save(chair);
        
        chairTreatmentRepository.deleteByDentalChair(chair);
        chairTreatmentRepository.flush();
        
        saveChairTreatments(chair, form.getTreatmentIds());
    }

    private void saveChairTreatments(DentalChair chair, List<Long> treatmentIds) {
        if (treatmentIds == null || treatmentIds.isEmpty()) {
            return;
        }
        for (Long treatmentId : treatmentIds) {
            TreatmentType treatmentType = treatmentTypeRepository.findById(treatmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid treatmentId"));
            
            if (!treatmentType.getDentalClinic().getDentalId().equals(chair.getDentalClinic().getDentalId())) {
                throw new IllegalArgumentException("Not authorized to access this treatment");
            }
            
            ChairTreatment ct = new ChairTreatment();
            ct.setDentalChair(chair);
            ct.setTreatmentType(treatmentType);
            chairTreatmentRepository.save(ct);
        }
    }

    public void deleteChair(Long chairId, String loginId) {
        DentalChair chair = getChairByIdAndLoginId(chairId, loginId);
        dentalChairRepository.delete(chair);
    }

    private DentalChairDto convertToDto(DentalChair chair) {
        DentalChairDto dto = new DentalChairDto();
        dto.setChairId(chair.getChairId());
        if (chair.getDentalClinic() != null) {
            dto.setDentalId(chair.getDentalClinic().getDentalId());
        }
        dto.setChairName(chair.getChairName());
        dto.setStatus(chair.getStatus());
        
        List<Long> treatmentIds = chairTreatmentRepository.findByDentalChair(chair).stream()
                .map(ct -> ct.getTreatmentType().getTreatmentId())
                .collect(Collectors.toList());
        dto.setTreatmentIds(treatmentIds);
        
        return dto;
    }
}
