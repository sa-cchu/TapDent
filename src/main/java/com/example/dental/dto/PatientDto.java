package com.example.dental.dto;

import com.example.dental.enums.PatientStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PatientDto {
    private Long patientId;
    private Long dentalId;
    private String patientCode;
    private String name;
    private LocalDate birthday;
    private String tel;
    private String email;
    private PatientStatus status;
    private Integer roleId;

    // 連続ログイン試行失敗回数
    private Integer loginAttempts;

    // アカウントロック解除日時
    private LocalDateTime lockedUntill;

    private Boolean deleteFrag;
}
