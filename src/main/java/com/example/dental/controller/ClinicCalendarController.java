package com.example.dental.controller;

import com.example.dental.dto.CalendarExceptionDto;
import com.example.dental.dto.DentalClinicDto;
import com.example.dental.form.CalendarExceptionForm;
import com.example.dental.service.CalendarExceptionService;
import com.example.dental.service.DentalClinicService;
import com.example.dental.service.DentistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/clinic/calendar")
@RequiredArgsConstructor
public class ClinicCalendarController {

    private final CalendarExceptionService calendarService;
    private final DentalClinicService clinicService;
    private final DentistService dentistService;

    @GetMapping
    public String showCalendar(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername();
        DentalClinicDto clinic = clinicService.getClinicByLoginId(loginId);
        model.addAttribute("clinicId", clinic.getDentalId());
        model.addAttribute("dentists", dentistService.getDentistsByLoginId(loginId));
        model.addAttribute("businessHours", clinicService.getBusinessHours(clinic.getDentalId()));
        return "clinic/calendar";
    }

    @GetMapping("/events")
    @ResponseBody
    public List<CalendarExceptionDto> getEvents(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        return calendarService.getEvents(clinic.getDentalId(), start, end);
    }

    @PostMapping("/holiday/toggle")
    @ResponseBody
    public CalendarExceptionDto toggleHoliday(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        return calendarService.toggleHoliday(clinic.getDentalId(), date);
    }

    @PostMapping("/event")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> saveEvent(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody @Valid CalendarExceptionForm form) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        try {
            CalendarExceptionDto saved = calendarService.saveDentistException(clinic.getDentalId(), form.toDto());
            return org.springframework.http.ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/event/{id}")
    @ResponseBody
    public void deleteEvent(@PathVariable Long id) {
        calendarService.deleteEvent(id);
    }
}
