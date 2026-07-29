package com.example.dental.form;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicBasicInfoForm {

    @NotBlank(message = "医院名は必須です")
    @Size(max = 50, message = "医院名は50文字以内で入力してください")
    private String name;

    @Size(max = 255, message = "住所は255文字以内で入力してください")
    private String address;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください")
    private String tel;

    @Email(message = "正しいメールアドレスの形式で入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String mail;

    @NotNull(message = "最大予約可能月数は必須です")
    @Min(value = 1, message = "1ヶ月以上を指定してください")
    @Max(value = 6, message = "6ヶ月以内を指定してください")
    private Integer maxReserveMonth;

    @NotNull(message = "当日予約制限の有無は必須です")
    private Boolean reservationRestrictions;

    @NotNull(message = "予約時間単位を選択してください")
    private Integer reservationTimeUnit;
    


    @Valid
    private List<BusinessHourForm> businessHours;
}
