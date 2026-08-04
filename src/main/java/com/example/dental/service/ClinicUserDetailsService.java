package com.example.dental.service;

import com.example.dental.entity.DentalClinic;
import com.example.dental.repository.DentalClinicRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security の認証に使用するサービス（歯科医院向け）。
 * loginId をもとに DB から DentalClinic を取得し、UserDetails を構築して返す。
 */
@Service
public class ClinicUserDetailsService implements UserDetailsService {

    private final DentalClinicRepository clinicRepository;

    public ClinicUserDetailsService(DentalClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        DentalClinic clinic = clinicRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "歯科医院アカウントが見つかりません: " + loginId));

        // ロール名をGrantedAuthorityに変換（例: ROLE_CLINIC）
        String roleName = clinic.getRole().getRoleName().name();

        return new com.example.dental.security.ClinicUserDetails(
                clinic.getLoginId(),
                clinic.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName)),
                clinic.getName());
    }
}
