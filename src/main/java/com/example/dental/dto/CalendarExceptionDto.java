package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CalendarExceptionDto {
    private Long calendarId;
    private Long dentalId;
    private LocalDate targetDate;
    private Boolean isHoliday;
    private Long chairId;
    private LocalTime startAt;
    private LocalTime endAt;
}
