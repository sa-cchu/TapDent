package com.example.dental.listener;

import com.example.dental.entity.DentalClinic;
import com.example.dental.enums.LogActionType;
import com.example.dental.security.ClinicUserDetails;
import com.example.dental.service.SystemLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final SystemLogService systemLogService;
    private final com.example.dental.repository.DentalClinicRepository dentalClinicRepository;

    public AuthenticationEventListener(SystemLogService systemLogService, com.example.dental.repository.DentalClinicRepository dentalClinicRepository) {
        this.systemLogService = systemLogService;
        this.dentalClinicRepository = dentalClinicRepository;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String loginId = "";
        DentalClinic clinic = null;
        String userType = "Unknown";

        if (principal instanceof ClinicUserDetails) {
            ClinicUserDetails userDetails = (ClinicUserDetails) principal;
            loginId = userDetails.getUsername();
            clinic = dentalClinicRepository.findByLoginId(loginId).orElse(null);
            userType = "Clinic";
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
            loginId = userDetails.getUsername();
            userType = "Admin";
        } else {
            // 他の認証（PatientLoginは手動処理のためここには来ない設計だが、万が一来た場合）
            return;
        }

        String ipAddress = getIpAddress(event.getAuthentication().getDetails());
        String description = userType + " ログイン成功";

        systemLogService.saveLogDirect(LogActionType.LOGIN_SUCCESS, loginId, clinic, description, ipAddress);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String loginId = event.getAuthentication().getName(); // 失敗時に入力されたID
        String ipAddress = getIpAddress(event.getAuthentication().getDetails());
        String exceptionMessage = event.getException().getMessage();
        
        String description = "ログイン失敗: " + exceptionMessage;

        // 失敗時はクリニック情報が確定できないためclinicはnull
        systemLogService.saveLogDirect(LogActionType.LOGIN_FAILURE, loginId, null, description, ipAddress);
    }

    private String getIpAddress(Object details) {
        if (details instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) details).getRemoteAddress();
        }
        return "unknown";
    }
}
