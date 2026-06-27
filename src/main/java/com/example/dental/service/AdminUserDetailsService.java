package com.example.dental.service;

import com.example.dental.entity.Admin;
import com.example.dental.repository.AdminRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security の認証に使用するサービス。
 * loginId をもとに DB から Admin を取得し、UserDetails を構築して返す。
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "管理者が見つかりません: " + loginId));

        // ロール名をGrantedAuthorityに変換（例: ROLE_ADMIN）
        String roleName = admin.getRole().getRoleName().name();

        return new User(
                admin.getLoginId(),
                admin.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}
