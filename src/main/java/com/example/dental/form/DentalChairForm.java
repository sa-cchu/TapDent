package com.example.dental.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DentalChairForm {

    @NotBlank(message = "チェア名を入力してください")
    @Size(max = 20, message = "チェア名は20文字以内で入力してください")
    private String chairName;

    @NotNull(message = "稼働状況を選択してください")
    private Boolean status = true;

    private java.util.List<Long> treatmentIds = new java.util.ArrayList<>();
}
