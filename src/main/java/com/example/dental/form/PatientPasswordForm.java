package com.example.dental.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientPasswordForm {

    @NotBlank(message = "現在のパスワードを入力してください")
    private String currentPassword;

    @NotBlank(message = "新しいパスワードを入力してください")
    private String newPassword;

    @NotBlank(message = "確認用の新しいパスワードを入力してください")
    private String confirmPassword;
}
