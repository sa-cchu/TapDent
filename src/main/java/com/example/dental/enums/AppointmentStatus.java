package com.example.dental.enums;

public enum AppointmentStatus {
    RESERVED,  // 予約確定
    ATTENDED,  // 来院済み
    CANCELLED, // キャンセル
    NOSHOW     // 無断キャンセル（不正予約制御に活用可能）
}