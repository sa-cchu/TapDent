package com.example.dental.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class ClinicUserDetails extends User {
    private final String clinicName;

    public ClinicUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String clinicName) {
        super(username, password, authorities);
        this.clinicName = clinicName;
    }

    public String getClinicName() {
        return clinicName;
    }
}
