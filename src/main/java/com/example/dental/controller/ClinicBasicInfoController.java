package com.example.dental.controller;

import com.example.dental.dto.BusinessHourDto;
import com.example.dental.dto.DentalClinicDto;
import com.example.dental.form.BusinessHourForm;
import com.example.dental.form.ClinicBasicInfoForm;
import com.example.dental.service.DentalClinicService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/clinic/basic-info")
public class ClinicBasicInfoController {

    private final DentalClinicService dentalClinicService;

    public ClinicBasicInfoController(DentalClinicService dentalClinicService) {
        this.dentalClinicService = dentalClinicService;
    }

    @GetMapping
    public String showBasicInfo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DentalClinicDto clinic = dentalClinicService.getClinicByLoginId(userDetails.getUsername());
        
        if (!model.containsAttribute("clinicBasicInfoForm")) {
            ClinicBasicInfoForm form = new ClinicBasicInfoForm();
            form.setName(clinic.getName());
            form.setAddress(clinic.getAddress());
            form.setTel(clinic.getTel());
            form.setMail(clinic.getMail());
            form.setMaxReserveMonth(clinic.getMaxReserveMonth() != null ? clinic.getMaxReserveMonth() : 3);
            form.setReservationRestrictions(clinic.getReservationRestrictions() != null ? clinic.getReservationRestrictions() : false);
            form.setLimitDentist(clinic.getLimitDentist() != null ? clinic.getLimitDentist() : 0);
            form.setLimitHygienist(clinic.getLimitHygienist() != null ? clinic.getLimitHygienist() : 0);
            form.setLimitOrthodontist(clinic.getLimitOrthodontist() != null ? clinic.getLimitOrthodontist() : 0);
            form.setLimitImplantologist(clinic.getLimitImplantologist() != null ? clinic.getLimitImplantologist() : 0);
            
            // 診療時間（月〜日）の初期化
            List<BusinessHourDto> existingHours = dentalClinicService.getBusinessHours(clinic.getDentalId());
            List<BusinessHourForm> bhForms = new ArrayList<>();
            
            for (DayOfWeek day : DayOfWeek.values()) {
                BusinessHourForm bhForm = new BusinessHourForm();
                bhForm.setDayOfWeek(day);
                bhForm.setDayOfWeekLabel(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
                
                // 既存のデータがあればセット
                BusinessHourDto existing = existingHours.stream()
                        .filter(bh -> bh.getDayOfWeek() == day)
                        .findFirst()
                        .orElse(null);
                        
                if (existing != null) {
                    bhForm.setOpenAt(existing.getOpenAt());
                    bhForm.setCloseAt(existing.getCloseAt());
                    bhForm.setBreakStartAt(existing.getBreakStartAt());
                    bhForm.setBreakEndAt(existing.getBreakEndAt());
                    bhForm.setRegularHoliday(existing.getRegularHoliday());
                } else {
                    bhForm.setRegularHoliday(false);
                }
                
                bhForms.add(bhForm);
            }
            form.setBusinessHours(bhForms);
            
            model.addAttribute("clinicBasicInfoForm", form);
        }
        
        model.addAttribute("clinic", clinic);
        return "clinic/basic-info";
    }

    @PostMapping("/update")
    public String updateBasicInfo(@AuthenticationPrincipal UserDetails userDetails,
                                  @Validated @ModelAttribute ClinicBasicInfoForm form,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        
        if (bindingResult.hasErrors()) {
            System.out.println("====== Binding Errors ======");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.toString());
            });
            System.out.println("============================");
            
            DentalClinicDto clinic = dentalClinicService.getClinicByLoginId(userDetails.getUsername());
            model.addAttribute("clinic", clinic);
            return "clinic/basic-info";
        }
        
        try {
            dentalClinicService.updateBasicInfo(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("successMessage", "基本医院情報を更新しました。");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "基本医院情報の更新に失敗しました。");
        }
        
        return "redirect:/clinic/basic-info";
    }
}
