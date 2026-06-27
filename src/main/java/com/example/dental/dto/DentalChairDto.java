package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DentalChairDto {
    private Long chairId;
    private Long dentalId;
    private String chairName;
    private Boolean status;
}
