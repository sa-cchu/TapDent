package com.example.dental.service;

import com.example.dental.dto.BusinessHourDto;

import com.example.dental.dto.DentalClinicDto;
import com.example.dental.entity.BusinessHour;
import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Role;
import com.example.dental.enums.ContractStatusName;
import com.example.dental.enums.RoleName;
import com.example.dental.form.ClinicBasicInfoForm;
import com.example.dental.form.ClinicEditForm;
import com.example.dental.form.ClinicRegistrationForm;
import com.example.dental.repository.BusinessHourRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DentalClinicService {

    private final DentalClinicRepository dentalClinicRepository;
    private final RoleRepository roleRepository;
    private final BusinessHourRepository businessHourRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DentalClinicService(DentalClinicRepository dentalClinicRepository,
            RoleRepository roleRepository,
            BusinessHourRepository businessHourRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.roleRepository = roleRepository;
        this.businessHourRepository = businessHourRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<DentalClinicDto> getClinicsPage(String name, ContractStatusName contractStatusName, Pageable pageable) {
        Page<DentalClinic> entityPage;
        if (name != null && !name.isBlank()) {
            if (contractStatusName != null) {
                entityPage = dentalClinicRepository.findByNameContainingAndContractStatus(name, contractStatusName,
                        pageable);
            } else {
                entityPage = dentalClinicRepository.findByNameContaining(name, pageable);
            }
        } else {
            if (contractStatusName != null) {
                entityPage = dentalClinicRepository.findByContractStatus(contractStatusName, pageable);
            } else {
                entityPage = dentalClinicRepository.findAll(pageable);
            }
        }

        return entityPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public DentalClinicDto getClinicById(Long id) {
        DentalClinic clinic = dentalClinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid clinic Id:" + id));
        return convertToDto(clinic);
    }

    @Transactional(readOnly = true)
    public boolean existsByLoginId(String loginId) {
        return dentalClinicRepository.findByLoginId(loginId).isPresent();
    }

    @Transactional(readOnly = true)
    public DentalClinicDto getClinicByLoginId(String loginId) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId:" + loginId));
        return convertToDto(clinic);
    }

    public void registerClinic(ClinicRegistrationForm form) {
        // ROLE_CLINIC ロールを取得
        Role clinicRole = roleRepository.findByRoleName(RoleName.ROLE_CLINIC)
                .orElseThrow(() -> new RuntimeException("Role ROLE_CLINIC not found"));

        // 新しい医院エンティティを生成して値をセット
        DentalClinic clinic = new DentalClinic();
        clinic.setLoginId(form.getLoginId());
        clinic.setPassword(passwordEncoder.encode(form.getPassword()));
        clinic.setName(form.getName());
        clinic.setAddress(form.getAddress());
        clinic.setMail(form.getMail());

        clinic.setContractStatus(form.getContractStatus());
        clinic.setRole(clinicRole);

        // 予約制限フラグはデフォルトで false
        clinic.setReservationRestrictions(false);

        // ランダムなURLトークンを生成
        clinic.setPublicUrlToken(UUID.randomUUID().toString());

        // 保存
        dentalClinicRepository.save(clinic);
    }

    public void updateClinic(Long id, ClinicEditForm form) {
        DentalClinic clinic = dentalClinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid clinic Id:" + id));

        clinic.setName(form.getName());
        clinic.setAddress(form.getAddress());
        clinic.setMail(form.getMail());

        clinic.setContractStatus(form.getContractStatus());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            clinic.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        dentalClinicRepository.save(clinic);
    }

    public void updateCredentials(String currentLoginId, String newLoginId, String newPassword) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId:" + currentLoginId));

        if (newLoginId != null && !newLoginId.isBlank()) {
            clinic.setLoginId(newLoginId);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            clinic.setPassword(passwordEncoder.encode(newPassword));
        }

        dentalClinicRepository.save(clinic);
    }

    public void updateBasicInfo(String loginId, ClinicBasicInfoForm form) {
        DentalClinic clinic = dentalClinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId:" + loginId));

        clinic.setName(form.getName());
        clinic.setAddress(form.getAddress());
        clinic.setTel(form.getTel());
        clinic.setMail(form.getMail());
        clinic.setMaxReserveMonth(form.getMaxReserveMonth());
        clinic.setReservationRestrictions(form.getReservationRestrictions());
        clinic.setLimitDentist(form.getLimitDentist() != null ? form.getLimitDentist() : 0);
        clinic.setLimitHygienist(form.getLimitHygienist() != null ? form.getLimitHygienist() : 0);
        clinic.setLimitOrthodontist(form.getLimitOrthodontist() != null ? form.getLimitOrthodontist() : 0);
        clinic.setLimitImplantologist(form.getLimitImplantologist() != null ? form.getLimitImplantologist() : 0);

        dentalClinicRepository.save(clinic);

        // 診療時間の更新
        if (form.getBusinessHours() != null) {
            for (com.example.dental.form.BusinessHourForm bhForm : form.getBusinessHours()) {
                BusinessHour bh = businessHourRepository
                        .findByDentalClinicDentalIdAndDayOfWeek(clinic.getDentalId(), bhForm.getDayOfWeek())
                        .orElse(new BusinessHour());

                bh.setDentalClinic(clinic);
                bh.setDayOfWeek(bhForm.getDayOfWeek());
                bh.setRegularHoliday(bhForm.getRegularHoliday() != null ? bhForm.getRegularHoliday() : false);

                if (bh.getRegularHoliday()) {
                    // 休診日の場合、時間はnullとして保存
                    bh.setOpenAt(null);
                    bh.setCloseAt(null);
                    bh.setBreakStartAt(null);
                    bh.setBreakEndAt(null);
                } else {
                    bh.setOpenAt(bhForm.getOpenAt());
                    bh.setCloseAt(bhForm.getCloseAt());
                    bh.setBreakStartAt(bhForm.getBreakStartAt());
                    bh.setBreakEndAt(bhForm.getBreakEndAt());
                }

                businessHourRepository.save(bh);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BusinessHourDto> getBusinessHours(Long dentalId) {
        return businessHourRepository.findByDentalClinicDentalId(dentalId).stream()
                .map(this::convertBusinessHourToDto)
                .toList();
    }

    private BusinessHourDto convertBusinessHourToDto(BusinessHour entity) {
        BusinessHourDto dto = new BusinessHourDto();
        dto.setBusinessId(entity.getBusinessId());
        if (entity.getDentalClinic() != null) {
            dto.setDentalId(entity.getDentalClinic().getDentalId());
        }
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setOpenAt(entity.getOpenAt());
        dto.setCloseAt(entity.getCloseAt());
        dto.setBreakStartAt(entity.getBreakStartAt());
        dto.setBreakEndAt(entity.getBreakEndAt());
        dto.setRegularHoliday(entity.getRegularHoliday());
        return dto;
    }

    public DentalClinicDto convertToDto(DentalClinic entity) {
        if (entity == null)
            return null;
        DentalClinicDto dto = new DentalClinicDto();
        dto.setDentalId(entity.getDentalId());
        dto.setLoginId(entity.getLoginId());
        dto.setPassword(entity.getPassword());
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setTel(entity.getTel());
        dto.setMail(entity.getMail());
        dto.setMaxReserveMonth(entity.getMaxReserveMonth());
        dto.setReservationRestrictions(entity.getReservationRestrictions());
        dto.setPublicUrlToken(entity.getPublicUrlToken());
        dto.setContractStatus(entity.getContractStatus());
        dto.setLimitDentist(entity.getLimitDentist());
        dto.setLimitHygienist(entity.getLimitHygienist());
        dto.setLimitOrthodontist(entity.getLimitOrthodontist());
        dto.setLimitImplantologist(entity.getLimitImplantologist());

        if (entity.getRole() != null) {
            dto.setRoleId(entity.getRole().getRoleId());
        }
        return dto;
    }
}
