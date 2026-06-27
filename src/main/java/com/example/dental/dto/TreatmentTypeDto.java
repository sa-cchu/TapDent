package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TreatmentTypeDto {
    private Long treatmentId;
    private Long dentalId;
    private String treatmentName;
    private Integer requiredMinutes;
    private Boolean status;
    private Boolean isExistingOnly;
}
