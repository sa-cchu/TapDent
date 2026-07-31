package com.example.dental.controller;

import com.example.dental.form.DentistForm;
import com.example.dental.service.DentistService;
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
@RequestMapping("/clinic/dentists")
public class ClinicDentistController {

    private final DentistService dentistService;
    private final TreatmentTypeService treatmentTypeService;

    public ClinicDentistController(DentistService dentistService, TreatmentTypeService treatmentTypeService) {
        this.dentistService = dentistService;
        this.treatmentTypeService = treatmentTypeService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("dentists", dentistService.getDentistsByLoginId(userDetails.getUsername()));
        return "clinic/dentists/list";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("dentistForm", new DentistForm());
        model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
        return "clinic/dentists/form";
    }

    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal UserDetails userDetails,
                               @Validated @ModelAttribute DentistForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
            return "clinic/dentists/form";
        }
        
        dentistService.saveDentist(userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "歯科医師を登録しました。");
        return "redirect:/clinic/dentists";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        var dentist = dentistService.getDentistDtoByIdAndLoginId(id, userDetails.getUsername());
        
        DentistForm form = new DentistForm();
        form.setDentistName(dentist.getDentistName());
        form.setStatus(dentist.getStatus());
        form.setTreatmentIds(dentist.getTreatmentIds());
        
        model.addAttribute("dentistForm", form);
        model.addAttribute("dentistId", id);
        model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
        
        return "clinic/dentists/form";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Validated @ModelAttribute DentistForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dentistId", id);
            model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
            return "clinic/dentists/form";
        }
        
        dentistService.updateDentist(id, userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "歯科医師情報を更新しました。");
        return "redirect:/clinic/dentists";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        dentistService.deleteDentist(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "歯科医師を削除しました。");
        return "redirect:/clinic/dentists";
    }
}
