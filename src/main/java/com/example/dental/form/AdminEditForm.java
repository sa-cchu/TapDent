package com.example.dental.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理者のユーザー名・パスワード編集フォーム
 */
public class AdminEditForm {

    /** 表示名（必須、最大20文字） */
    @NotBlank(message = "名前を入力してください")
    @Size(max = 20, message = "名前は20文字以内で入力してください")
    private String name;

    /** 現在のパスワード（変更時に本人確認として使用） */
    @NotBlank(message = "現在のパスワードを入力してください")
    private String currentPassword;

    /** 新しいパスワード（省略可能、入力時は8文字以上） */
    @Size(min = 8, max = 100, message = "新しいパスワードは8文字以上で入力してください")
    private String newPassword;

    /** 新しいパスワード（確認用） */
    private String confirmPassword;

    // --- Getter / Setter ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
