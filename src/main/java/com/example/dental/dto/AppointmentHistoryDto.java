package com.example.dental.dto;

import com.example.dental.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentHistoryDto {
    private Long appointmentId;
    private Long dentalId;
    private Long chairId;
    private Long patientId;
    private Long tokenId;
    private Long treatmentId;
    private Boolean appointMethod;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AppointmentStatus status;
    private LocalDateTime archiveAt;
}
