package com.example.dental.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DentistForm {

    @NotBlank(message = "歯科医師名を入力してください")
    @Size(max = 20, message = "歯科医師名は20文字以内で入力してください")
    private String dentistName;

    @NotNull(message = "ステータスを選択してください")
    private Boolean status;

    private List<Long> treatmentIds;
}
