package com.example.dental.controller;

import com.example.dental.form.TreatmentTypeForm;
import com.example.dental.service.TreatmentTypeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clinic/treatments")
public class ClinicTreatmentController {

    private final TreatmentTypeService treatmentTypeService;

    public ClinicTreatmentController(TreatmentTypeService treatmentTypeService) {
        this.treatmentTypeService = treatmentTypeService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) com.example.dental.enums.TargetPatientType targetPatient,
                       @AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), targetPatient));
        model.addAttribute("selectedTargetPatient", targetPatient);
        return "clinic/treatments/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("treatmentTypeForm", new TreatmentTypeForm());
        return "clinic/treatments/form";
    }

    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal UserDetails userDetails,
                               @Validated @ModelAttribute TreatmentTypeForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "clinic/treatments/form";
        }
        
        treatmentTypeService.saveTreatment(userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "診療メニューを登録しました。");
        return "redirect:/clinic/treatments";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        var treatment = treatmentTypeService.getTreatmentByIdAndLoginId(id, userDetails.getUsername());
        
        TreatmentTypeForm form = new TreatmentTypeForm();
        form.setTreatmentName(treatment.getTreatmentName());
        form.setRequiredMinutes(treatment.getRequiredMinutes());
        form.setStatus(treatment.getStatus());
        form.setTargetPatientType(treatment.getTargetPatientType());
        form.setTargetStaffType(treatment.getTargetStaffType());
        
        model.addAttribute("treatmentTypeForm", form);
        model.addAttribute("treatmentId", id);
        
        return "clinic/treatments/form";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Validated @ModelAttribute TreatmentTypeForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("treatmentId", id);
            return "clinic/treatments/form";
        }
        
        treatmentTypeService.updateTreatment(id, userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "診療メニューを更新しました。");
        return "redirect:/clinic/treatments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        treatmentTypeService.deleteTreatment(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "診療メニューを削除しました。");
        return "redirect:/clinic/treatments";
    }
}
