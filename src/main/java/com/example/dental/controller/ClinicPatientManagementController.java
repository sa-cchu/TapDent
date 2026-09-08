package com.example.dental.controller;

import com.example.dental.dto.DentalClinicDto;
import com.example.dental.dto.PatientDetailDto;
import com.example.dental.dto.PatientDto;
import com.example.dental.form.ClinicPatientEditForm;
import com.example.dental.service.ClinicPatientManagementService;
import com.example.dental.service.DentalClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clinic/patients")
@RequiredArgsConstructor
public class ClinicPatientManagementController {

    private final ClinicPatientManagementService patientManagementService;
    private final DentalClinicService clinicService;
    private final com.example.dental.repository.DentalClinicRepository dentalClinicRepository;
    private final com.example.dental.service.SystemLogService systemLogService;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        Model model) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        List<PatientDto> patients = patientManagementService.searchPatients(clinic.getDentalId(), keyword);
        
        model.addAttribute("patients", patients);
        model.addAttribute("keyword", keyword);
        return "clinic/patients/index";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable("id") Long patientId,
                         Model model) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        PatientDetailDto detail = patientManagementService.getPatientDetail(clinic.getDentalId(), patientId);
        
        model.addAttribute("detail", detail);
        
        // 編集用フォームの初期化
        if (!model.containsAttribute("editForm")) {
            ClinicPatientEditForm form = new ClinicPatientEditForm();
            form.setPatientCode(detail.getPatient().getPatientCode());
            form.setName(detail.getPatient().getName());
            form.setPronunciationGuide(detail.getPatient().getPronunciationGuide()); // 注：PatientDtoにpronunciationGuideを追加する必要があるか確認
            form.setBirthday(detail.getPatient().getBirthday());
            form.setGender(detail.getPatient().getGender()); // 注：PatientDtoにGenderを追加する必要があるか確認
            form.setTel(detail.getPatient().getTel());
            form.setStatus(detail.getPatient().getStatus());
            model.addAttribute("editForm", form);
        }
        
        return "clinic/patients/detail";
    }

    @PostMapping("/{id}/edit")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable("id") Long patientId,
                         @Valid @ModelAttribute("editForm") ClinicPatientEditForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editForm", result);
            redirectAttributes.addFlashAttribute("editForm", form);
            redirectAttributes.addFlashAttribute("hasErrors", true);
            return "redirect:/clinic/patients/" + patientId;
        }

        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        try {
            patientManagementService.updatePatient(clinic.getDentalId(), patientId, form);
            redirectAttributes.addFlashAttribute("successMessage", "患者情報を更新しました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("editForm", form);
        }

        return "redirect:/clinic/patients/" + patientId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable("id") Long patientId,
                         RedirectAttributes redirectAttributes,
                         jakarta.servlet.http.HttpServletRequest request) {
        DentalClinicDto clinic = clinicService.getClinicByLoginId(userDetails.getUsername());
        try {
            patientManagementService.deletePatient(clinic.getDentalId(), patientId);
            
            com.example.dental.entity.DentalClinic entity = dentalClinicRepository.findById(clinic.getDentalId()).orElse(null);
            systemLogService.saveLog(com.example.dental.enums.LogActionType.USER_DELETE, userDetails.getUsername(), entity, "医院から患者を削除 (ID: " + patientId + ")", request);
            
            redirectAttributes.addFlashAttribute("successMessage", "患者を削除しました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/clinic/patients/" + patientId;
        }

        return "redirect:/clinic/patients";
    }
}
