package com.example.dental.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class PatientAccountForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "お名前を入力してください")
    @Size(max = 51, message = "お名前は51文字以内で入力してください")
    private String name;

    @NotBlank(message = "フリガナを入力してください")
    @Size(max = 50, message = "フリガナは50文字以内で入力してください")
    @Pattern(regexp = "^[ァ-ヶー]+$", message = "フリガナは全角カタカナで入力してください")
    private String pronunciationGuide;

    @NotNull(message = "生年月日を選択してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank(message = "性別を選択してください")
    private String gender;

    @NotBlank(message = "電話番号を入力してください")
    @Pattern(regexp = "^(090|080|070)[0-9]{8}$", message = "携帯電話番号（090, 080, 070から始まる11桁）で入力してください")
    private String tel;

    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "正しいメールアドレス形式で入力してください")
    private String email;

    // メールアドレスが変更された場合の認証コード用
    private String verificationCode;
}
