package com.example.dental.service;

import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.SystemLog;
import com.example.dental.enums.LogActionType;
import com.example.dental.repository.SystemLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * システムログを記録します（リクエストからIPアドレスを自動取得）。
     */
    @Transactional
    public void saveLog(LogActionType actionType, String loginId, DentalClinic dentalClinic, String description, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        saveLogDirect(actionType, loginId, dentalClinic, description, ipAddress);
    }

    /**
     * システムログを記録します（IPアドレスを直接指定）。
     */
    @Transactional
    public void saveLogDirect(LogActionType actionType, String loginId, DentalClinic dentalClinic, String description, String ipAddress) {
        SystemLog log = new SystemLog();
        log.setActionType(actionType);
        log.setLoginId(loginId);
        log.setDentalClinic(dentalClinic);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        
        systemLogRepository.save(log);
    }

    /**
     * リクエストからクライアントのIPアドレスを取得します。
     * ロードバランサーやプロキシ環境下も考慮します。
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
