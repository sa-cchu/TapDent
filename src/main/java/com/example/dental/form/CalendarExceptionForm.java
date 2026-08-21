package com.example.dental.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import com.example.dental.dto.CalendarExceptionDto;
import com.example.dental.enums.ExceptionType;

@Data
public class CalendarExceptionForm {
    private Long calendarId;
    @NotNull private LocalDate targetDate;
    private ExceptionType type = ExceptionType.HOLIDAY;
    private Long dentistId;
    private LocalTime startAt;
    private LocalTime endAt;
    private LocalTime breakStartAt;
    private LocalTime breakEndAt;
    private Boolean isAllDay = true;

    public CalendarExceptionDto toDto() {
        CalendarExceptionDto dto = new CalendarExceptionDto();
        dto.setCalendarId(this.calendarId);
        dto.setTargetDate(this.targetDate);
        dto.setType(this.type);
        dto.setDentistId(this.dentistId);
        dto.setStartAt(this.startAt);
        dto.setEndAt(this.endAt);
        dto.setBreakStartAt(this.breakStartAt);
        dto.setBreakEndAt(this.breakEndAt);
        dto.setIsAllDay(this.isAllDay);
        return dto;
    }
}
