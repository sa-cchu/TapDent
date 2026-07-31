package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DentistDto {
    private Long dentistId;
    private Long dentalId;
    private String dentistName;
    private Boolean status;
    private java.util.List<Long> treatmentIds;
}
