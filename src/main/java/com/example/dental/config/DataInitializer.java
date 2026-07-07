package com.example.dental.config;

import com.example.dental.entity.Admin;
import com.example.dental.entity.Role;
import com.example.dental.enums.RoleName;
import com.example.dental.repository.AdminRepository;
import com.example.dental.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // パスワード暗号化用
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, AdminRepository adminRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. ロールの初期化（既存のコード）
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
            }
        }


        // 2. 本番用：初期管理者の自動生成ロジック
        if (adminRepository.count() == 0) {
            // ランダムな仮パスワードを生成（例: 8文字）
            String rawPassword = UUID.randomUUID().toString().substring(0, 8);

            // 最高管理者のロールを取得
            Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            Admin admin = new Admin();
            admin.setLoginId("admin_master");
            admin.setName("最高管理者");
            admin.setRole(adminRole);
            // Java側で安全にハッシュ化して保存
            admin.setPassword(passwordEncoder.encode(rawPassword));

            adminRepository.save(admin);

            // 【重要】サーバーのコンソールログにだけ、このパスワードを出力する
            System.out.println("===============================================");
            System.out.println("[⚠️初回起動システムログ] 初期管理者アカウントを作成しました。");
            System.out.println("ログインID: admin_master");
            System.out.println("初期パスワード: " + rawPassword);
            System.out.println("※ログイン後、速やかにパスワードを変更してください。");
            System.out.println("===============================================");
        }
    }
}