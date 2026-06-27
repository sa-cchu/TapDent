package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class BusinessHourDto {
    private Long businessId;
    private Long dentalId;
    private DayOfWeek dayOfWeek;
    private LocalTime openAt;
    private LocalTime closeAt;
    private LocalTime breakStartAt;
    private LocalTime breakEndAt;
    private Boolean regularHoliday;
}
