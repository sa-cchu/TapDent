package com.example.dental.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;
import com.example.dental.enums.TargetPatientType;

@Data
public class TreatmentTypeForm {

    @NotBlank(message = "診療メニュー名を入力してください")
    private String treatmentName;

    @NotNull(message = "所要時間を入力してください")
    private Integer requiredMinutes;

    @NotNull(message = "公開状況を選択してください")
    private Boolean status;

    @NotEmpty(message = "対象患者を1つ以上選択してください")
    private List<TargetPatientType> targetPatientTypes;

}
