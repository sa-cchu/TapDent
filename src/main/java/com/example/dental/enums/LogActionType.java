package com.example.dental.enums;

/**
 * システムログのアクション種別
 */
public enum LogActionType {
    LOGIN_SUCCESS,       // ログイン成功
    LOGIN_FAILURE,       // ログイン失敗
    PASSWORD_CHANGE,     // パスワード変更
    APPOINTMENT_CREATE,  // 予約作成
    APPOINTMENT_UPDATE,  // 予約更新
    APPOINTMENT_CANCEL,  // 予約キャンセル
    USER_CREATE,         // ユーザー作成
    USER_DELETE,         // ユーザー削除
    ROLE_UPDATE,         // 権限更新
    CLINIC_SETTING_UPDATE // 医院設定更新
}
