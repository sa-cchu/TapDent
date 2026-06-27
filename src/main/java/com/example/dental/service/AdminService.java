package com.example.dental.service;

import com.example.dental.dto.AdminDto;
import com.example.dental.entity.Admin;
import com.example.dental.form.AdminEditForm;
import com.example.dental.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminDto getAdminByLoginId(String loginId) {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + loginId));
        return convertToDto(admin);
    }

    public void updateAdminProfile(String loginId, AdminEditForm form) {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + loginId));

        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(form.getNewPassword()));
        }
        admin.setName(form.getName());
        adminRepository.save(admin);
    }

    public AdminDto convertToDto(Admin entity) {
        if (entity == null) return null;
        AdminDto dto = new AdminDto();
        dto.setAdminId(entity.getAdminId());
        dto.setLoginId(entity.getLoginId());
        dto.setPassword(entity.getPassword());
        dto.setName(entity.getName());
        if (entity.getRole() != null) {
            com.example.dental.dto.RoleDto roleDto = new com.example.dental.dto.RoleDto();
            roleDto.setRoleId(entity.getRole().getRoleId());
            roleDto.setRoleName(entity.getRole().getRoleName());
            dto.setRole(roleDto);
        }
        return dto;
    }
}
