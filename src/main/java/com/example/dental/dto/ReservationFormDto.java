package com.example.dental.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.dental.enums.AppointMethod;
import com.example.dental.enums.VisitType;

import lombok.Data;

@Data
public class ReservationFormDto {
    private AppointMethod appointMethod;
    private VisitType visitType;
    
    // For Existing Patient
    private Long patientId;
    
    // For New Patient
    private String name;
    private String nameKana;
    private String tel;
    
    // Appointment Details
    private Long treatmentId;
    
    // Optional (null means auto-assign)
    private Long dentistId;
    private Long chairId;
    
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    
    private String patientComment;
}
