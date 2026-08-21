package com.example.dental.dto;

import com.example.dental.enums.AppointMethod;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.enums.VisitType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentHistoryDto {
    private Long appointmentId;
    private Long dentalId;
    private Long chairId;
    private Long dentistId;
    private Long patientId;
    private Long treatmentId;
    private AppointMethod appointMethod;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String patientComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AppointmentStatus status;
    private VisitType visitType;
    private LocalDateTime archiveAt;
}
