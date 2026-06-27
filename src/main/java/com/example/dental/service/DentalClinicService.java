package com.example.dental.service;

import com.example.dental.dto.ContractStatusDto;
import com.example.dental.dto.DentalClinicDto;
import com.example.dental.entity.ContractStatus;
import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Role;
import com.example.dental.enums.ContractStatusName;
import com.example.dental.enums.RoleName;
import com.example.dental.form.ClinicEditForm;
import com.example.dental.form.ClinicRegistrationForm;
import com.example.dental.repository.ContractStatusRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DentalClinicService {

    private final DentalClinicRepository dentalClinicRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DentalClinicService(DentalClinicRepository dentalClinicRepository,
                               ContractStatusRepository contractStatusRepository,
                               RoleRepository roleRepository,
                               BCryptPasswordEncoder passwordEncoder) {
        this.dentalClinicRepository = dentalClinicRepository;
        this.contractStatusRepository = contractStatusRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<DentalClinicDto> getClinicsPage(String name, ContractStatusName contractStatusName, Pageable pageable) {
        ContractStatus statusEntity = null;
        if (contractStatusName != null) {
            statusEntity = contractStatusRepository.findByStatusName(contractStatusName).orElse(null);
        }

        Page<DentalClinic> entityPage;
        if (name != null && !name.isBlank()) {
            if (statusEntity != null) {
                entityPage = dentalClinicRepository.findByNameContainingAndContractStatus(name, statusEntity, pageable);
            } else {
                entityPage = dentalClinicRepository.findByNameContaining(name, pageable);
            }
        } else {
            if (statusEntity != null) {
                entityPage = dentalClinicRepository.findByContractStatus(statusEntity, pageable);
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

        ContractStatus contractStatus = contractStatusRepository.findByStatusName(form.getContractStatus())
                .orElseThrow(() -> new RuntimeException("ContractStatus not found"));
        clinic.setContractStatus(contractStatus);
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

        ContractStatus contractStatus = contractStatusRepository.findByStatusName(form.getContractStatus())
                .orElseThrow(() -> new RuntimeException("ContractStatus not found"));
        clinic.setContractStatus(contractStatus);

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            clinic.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        dentalClinicRepository.save(clinic);
    }

    public DentalClinicDto convertToDto(DentalClinic entity) {
        if (entity == null) return null;
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
        if (entity.getContractStatus() != null) {
            ContractStatusDto statusDto = new ContractStatusDto();
            statusDto.setStatusId(entity.getContractStatus().getStatusId());
            statusDto.setStatusName(entity.getContractStatus().getStatusName());
            dto.setContractStatus(statusDto);
        }
        if (entity.getRole() != null) {
            dto.setRoleId(entity.getRole().getRoleId());
        }
        return dto;
    }
}
