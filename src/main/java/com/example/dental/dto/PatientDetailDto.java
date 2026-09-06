package com.example.dental.dto;

import lombok.Data;
import java.util.List;

@Data
public class PatientDetailDto {
    private PatientDto patient;
    private List<AppointmentDto> appointments;
}
