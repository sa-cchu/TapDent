package com.example.dental.dto;

import com.example.dental.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentDto {
    private Long appointmentId;
    private Long dentalId;
    private Long chairId;
    private Long patientId;
    private Long treatmentId;
    private Boolean appointMethod;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime updateAt;
    private AppointmentStatus status;
}
