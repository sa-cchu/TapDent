package com.example.dental.form;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.dental.validation.ValidBusinessHour;
import lombok.Data;

@Data
@ValidBusinessHour
public class BusinessHourForm {
    
    private DayOfWeek dayOfWeek;
    
    private String dayOfWeekLabel; // 表示用（月、火など）
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openAt;
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime closeAt;
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakStartAt;
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakEndAt;
    
    private Boolean regularHoliday = false;
}
