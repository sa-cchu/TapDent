package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import com.example.dental.enums.ExceptionType;
import java.time.LocalTime;

@Getter
@Setter
public class CalendarExceptionDto {
    private Long calendarId;
    private Long dentalId;
    private LocalDate targetDate;
    private ExceptionType type;

    private Long dentistId;    // null => 全院休診、非null => 歯科医師の休み
    private String dentistName; // transient, for UI display
    private LocalTime startAt; // 必須 (dentistId != null)
    private LocalTime endAt;   // 必須 (dentistId != null)
    private LocalTime breakStartAt;
    private LocalTime breakEndAt;
    private Boolean isAllDay = true;
}
