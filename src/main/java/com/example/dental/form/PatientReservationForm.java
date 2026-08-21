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
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class PatientReservationForm implements Serializable {
    private static final long serialVersionUID = 1L;

    // Step 1: 診療メニュー
    @NotNull(message = "診療メニューを選択してください", groups = Step1.class)
    private Long treatmentId;
    
    // 画面表示用
    private String treatmentName;
    private Integer treatmentDurationMinutes;

    // Step 2: 日時
    @NotNull(message = "予約日を選択してください", groups = Step2.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @NotNull(message = "予約時間を選択してください", groups = Step2.class)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    // Step 3: 患者情報
    @NotBlank(message = "お名前を入力してください", groups = Step3.class)
    @Size(max = 51, message = "お名前は51文字以内で入力してください", groups = Step3.class)
    private String name;

    @NotBlank(message = "フリガナを入力してください", groups = Step3.class)
    @Size(max = 50, message = "フリガナは50文字以内で入力してください", groups = Step3.class)
    @Pattern(regexp = "^[ァ-ヶー]+$", message = "フリガナは全角カタカナで入力してください", groups = Step3.class)
    private String pronunciationGuide;

    @NotNull(message = "生年月日を選択してください", groups = Step3.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank(message = "性別を選択してください", groups = Step3.class)
    private String gender;

    @NotBlank(message = "電話番号を入力してください", groups = Step3.class)
    @Pattern(regexp = "^(090|080|070)[0-9]{8}$", message = "携帯電話番号（090, 080, 070から始まる11桁）で入力してください", groups = Step3.class)
    private String tel;

    @NotBlank(message = "メールアドレスを入力してください", groups = Step3.class)
    @Email(message = "正しいメールアドレス形式で入力してください", groups = Step3.class)
    private String email;

    // 既存患者用：診察券番号（任意）
    private String patientCode;

    // パスワードは生年月日から自動生成するためフロントエンドからの入力は不要
    private String password;

    private String patientComment;

    // Step 4: 認証情報
    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;

    @NotBlank(message = "認証コードを入力してください", groups = Step4.class)
    private String inputVerificationCode;

    // Validation groups
    public interface Step1 {}
    public interface Step2 {}
    public interface Step3 {}
    public interface Step4 {}
}
