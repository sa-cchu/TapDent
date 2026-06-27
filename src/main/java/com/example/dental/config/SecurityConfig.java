package com.example.dental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードハッシュ化用のBeanを登録。
     * DataInitializerなど、各コンポーネントからDIして使用する。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager を Bean として公開（必要に応じて使用可能）
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Security フィルターチェーンの設定
     * - /admin/login のみ未認証アクセスを許可
     * - /admin/** はすべて認証必須
     * - フォームログイン・ログアウトを有効化
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 静的リソースは全員アクセス可
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // ログインページは未認証でアクセス可
                .requestMatchers("/admin/login").permitAll()
                // /admin/** はすべて認証必須
                .requestMatchers("/admin/**").authenticated()
                // その他は全員アクセス可（患者向けページなど将来追加分）
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")              // カスタムログインページ
                .loginProcessingUrl("/admin/login")     // POST 先（Spring Security が処理）
                .defaultSuccessUrl("/admin/dashboard", true) // 認証成功後のリダイレクト先
                .failureUrl("/admin/login?error")       // 認証失敗後のリダイレクト先
                .usernameParameter("loginId")           // フォームのユーザー名フィールド名
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
