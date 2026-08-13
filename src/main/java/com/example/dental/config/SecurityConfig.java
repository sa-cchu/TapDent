package com.example.dental.config;

import com.example.dental.service.AdminUserDetailsService;
import com.example.dental.service.ClinicUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;
    private final ClinicUserDetailsService clinicUserDetailsService;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService, ClinicUserDetailsService clinicUserDetailsService) {
        this.adminUserDetailsService = adminUserDetailsService;
        this.clinicUserDetailsService = clinicUserDetailsService;
    }

    /**
     * パスワードハッシュ化用のBeanを登録。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 管理者(Admin)向けのSecurityFilterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider adminAuthProvider = new DaoAuthenticationProvider(adminUserDetailsService);
        adminAuthProvider.setPasswordEncoder(passwordEncoder());

        http.securityMatcher("/admin/**")
            .authenticationProvider(adminAuthProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .requestMatchers("/admin/**").authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error")
                .usernameParameter("loginId")
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

    /**
     * 歯科医院(Clinic)向けのSecurityFilterChain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain clinicFilterChain(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider clinicAuthProvider = new DaoAuthenticationProvider(clinicUserDetailsService);
        clinicAuthProvider.setPasswordEncoder(passwordEncoder());

        http.securityMatcher("/clinic/**")
            .authenticationProvider(clinicAuthProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/clinic/login").permitAll()
                .requestMatchers("/clinic/**").authenticated()
            )
            .formLogin(form -> form
                .loginPage("/clinic/login")
                .loginProcessingUrl("/clinic/login")
                .defaultSuccessUrl("/clinic/dashboard", true)
                .failureUrl("/clinic/login?error")
                .usernameParameter("loginId")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/clinic/logout")
                .logoutSuccessUrl("/clinic/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * 患者(Patient)向けのSecurityFilterChain
     * トークンベースの動的URLのため、未認証時はパスからトークンを抽出してログイン画面へリダイレクトする
     */
    @Bean
    @Order(3)
    public SecurityFilterChain patientFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/patient/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/patient/*/login", "/patient/*/register").permitAll()
                .requestMatchers("/patient/**").authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    String[] parts = uri.split("/");
                    if (parts.length >= 3 && "patient".equals(parts[1])) {
                        String token = parts[2];
                        response.sendRedirect("/patient/" + token + "/login");
                    } else {
                        response.sendRedirect("/");
                    }
                })
            );

        return http.build();
    }

    /**
     * その他のアクセス(静的リソースなど)向けのSecurityFilterChain
     */
    @Bean
    @Order(4)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
            .anyRequest().permitAll()
        );
        return http.build();
    }
}
