package com.example.dental.form;
import jakarta.validation.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import com.example.dental.enums.PatientStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ClinicPatientEditForm {

    private String patientCode;

    @NotBlank(message = "氏名を入力してください")
    private String name;

    @NotBlank(message = "フリガナを入力してください")
    private String pronunciationGuide;

    @NotNull(message = "生年月日を選択してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank(message = "性別を選択してください")
    private String gender;

    @NotBlank(message = "電話番号を入力してください")
    @Pattern(regexp = "^[0-9-]+$", message = "電話番号は数字とハイフンのみで入力してください")
    private String tel;

    @NotNull(message = "ステータスを選択してください")
    private PatientStatus status;
}
