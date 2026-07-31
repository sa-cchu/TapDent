package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DentistTreatmentDto {
    private Long dtId;
    private Long dentistId;
    private Long treatmentId;
}
