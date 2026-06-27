package com.example.dental.enums;

public enum RoleName {
    ROLE_PATIENT, // 患者（予約の取得・閲覧のみ）
    ROLE_CLINIC,  // 歯科医院（自院の予約管理・設定）
    ROLE_ADMIN    // システム管理者（全医院の管理・お知らせ一括配信など）
}