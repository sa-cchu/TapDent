package com.example.dental.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.dental.enums.PatientStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
// 複合ユニーク制約（歯科医院ID と 診察番号 の組み合わせの重複を防ぐ）を定義
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(name = "uk_dental_patient_code", columnNames = {
        "dental_id", "patient_code" }))
@Getter
@Setter
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId;

    // 歯科医院情報との多対一の紐付け（外部キー: dental_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    @Column(name = "patient_code", length = 20)
    private String patientCode;

    @Column(name = "pronunciation_guide", nullable = false, length = 50)
    private String pronunciationGuide;

    @Column(nullable = false, length = 51)
    private String name;

    // 生年月日（時分情報は不要なため LocalDate を採用）
    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false, length = 20)
    private String tel;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // Enumとして管理（DBには文字列として保存）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PatientStatus status;

    // 患者アカウントに権限を紐付け
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // 連続ログイン試行失敗回数
    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    // アカウントロック解除日時
    @Column(name = "locked_untill", nullable = false)
    private LocalDateTime lockedUntill;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}