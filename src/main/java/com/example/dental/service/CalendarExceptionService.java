package com.example.dental.service;

import com.example.dental.dto.CalendarExceptionDto;
import com.example.dental.entity.CalendarException;
import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Dentist;
import com.example.dental.repository.CalendarExceptionRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.DentistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarExceptionService {

    private final CalendarExceptionRepository repository;
    private final DentalClinicRepository clinicRepository;
    private final DentistRepository dentistRepository;

    @Transactional(readOnly = true)
    public List<CalendarExceptionDto> getEvents(Long clinicId, LocalDate start, LocalDate end) {
        DentalClinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid clinic ID"));
        List<CalendarException> events = repository.findByDentalClinicAndTargetDateBetween(clinic, start, end);
        return events.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CalendarExceptionDto toggleHoliday(Long clinicId, LocalDate date) {
        DentalClinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid clinic ID"));
        
        List<CalendarException> existing = repository.findByDentalClinicAndDentistIsNullAndTargetDateBetween(clinic, date, date);
        if (!existing.isEmpty()) {
            repository.deleteAll(existing);
            return null; // Toggled off
        } else {
            CalendarException ex = new CalendarException();
            ex.setDentalClinic(clinic);
            ex.setTargetDate(date);
            ex.setType(com.example.dental.enums.ExceptionType.HOLIDAY);
            ex = repository.save(ex);
            return toDto(ex);
        }
    }

    @Transactional
    public CalendarExceptionDto saveDentistException(Long clinicId, CalendarExceptionDto dto) {
        DentalClinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid clinic ID"));
        
        CalendarException ex;
        if (dto.getCalendarId() != null) {
            ex = repository.findById(dto.getCalendarId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid calendar ID"));
        } else {
            ex = new CalendarException();
            ex.setDentalClinic(clinic);
        }
        
        // 全院対象の場合の同日重複タイプチェック
        if (dto.getDentistId() == null) {
            com.example.dental.enums.ExceptionType newType = dto.getType() != null ? dto.getType() : com.example.dental.enums.ExceptionType.HOLIDAY;
            List<CalendarException> existingAllClinic = repository.findByDentalClinicAndDentistIsNullAndTargetDateBetween(clinic, dto.getTargetDate(), dto.getTargetDate());
            for (CalendarException e : existingAllClinic) {
                if (dto.getCalendarId() != null && e.getCalendarId().equals(dto.getCalendarId())) {
                    continue; // 自分自身の更新はスキップ
                }
                if (e.getType() != newType) {
                    throw new IllegalArgumentException("同日に「全院休診」と「全院特別診療」の両方を登録することはできません。");
                }
            }
        }
        
        ex.setTargetDate(dto.getTargetDate());
        ex.setType(dto.getType() != null ? dto.getType() : com.example.dental.enums.ExceptionType.HOLIDAY);
        
        if (dto.getDentistId() != null) {
            Dentist dentist = dentistRepository.findById(dto.getDentistId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid dentist ID"));
            ex.setDentist(dentist);
            ex.setStartAt(dto.getStartAt());
            ex.setEndAt(dto.getEndAt());
            ex.setBreakStartAt(dto.getBreakStartAt());
            ex.setBreakEndAt(dto.getBreakEndAt());
        } else {
            ex.setDentist(null);
            ex.setStartAt(dto.getStartAt()); // Allow times for special consultation without dentist
            ex.setEndAt(dto.getEndAt());
            ex.setBreakStartAt(dto.getBreakStartAt());
            ex.setBreakEndAt(dto.getBreakEndAt());
        }
        
        ex = repository.save(ex);
        return toDto(ex);
    }

    @Transactional
    public void deleteEvent(Long id) {
        repository.deleteById(id);
    }
    
    private CalendarExceptionDto toDto(CalendarException entity) {
        CalendarExceptionDto dto = new CalendarExceptionDto();
        dto.setCalendarId(entity.getCalendarId());
        dto.setDentalId(entity.getDentalClinic().getDentalId());
        dto.setTargetDate(entity.getTargetDate());
        dto.setType(entity.getType());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setBreakStartAt(entity.getBreakStartAt());
        dto.setBreakEndAt(entity.getBreakEndAt());
        
        if (entity.getDentist() != null) {
            dto.setDentistId(entity.getDentist().getDentistId());
            dto.setDentistName(entity.getDentist().getDentistName());
        }
        return dto;
    }
}
