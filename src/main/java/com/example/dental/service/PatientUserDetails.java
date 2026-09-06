package com.example.dental.service;

import com.example.dental.entity.Patient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class PatientUserDetails implements UserDetails {

    private final Patient patient;

    public PatientUserDetails(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // PatientエンティティのRoleに基づいた権限を付与
        String roleName = patient.getRole() != null ? patient.getRole().getRoleName().name() : "ROLE_PATIENT";
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return patient.getPassword();
    }

    @Override
    public String getUsername() {
        return patient.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return patient.getLockedUntill() == null || patient.getLockedUntill().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !patient.getIsDeleted() && patient.getStatus() != com.example.dental.enums.PatientStatus.WITHDRAWN;
    }
}
