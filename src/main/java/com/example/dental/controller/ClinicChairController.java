package com.example.dental.controller;

import com.example.dental.form.DentalChairForm;
import com.example.dental.service.DentalChairService;
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
@RequestMapping("/clinic/chairs")
public class ClinicChairController {

    private final DentalChairService dentalChairService;
    private final TreatmentTypeService treatmentTypeService;

    public ClinicChairController(DentalChairService dentalChairService, TreatmentTypeService treatmentTypeService) {
        this.dentalChairService = dentalChairService;
        this.treatmentTypeService = treatmentTypeService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("chairs", dentalChairService.getChairsByLoginId(userDetails.getUsername()));
        return "clinic/chairs/list";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("dentalChairForm", new DentalChairForm());
        model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
        return "clinic/chairs/form";
    }

    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal UserDetails userDetails,
                               @Validated @ModelAttribute DentalChairForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
            return "clinic/chairs/form";
        }
        
        dentalChairService.saveChair(userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "チェアを登録しました。");
        return "redirect:/clinic/chairs";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        var chair = dentalChairService.getChairDtoByIdAndLoginId(id, userDetails.getUsername());
        
        DentalChairForm form = new DentalChairForm();
        form.setChairName(chair.getChairName());
        form.setStatus(chair.getStatus());
        form.setTreatmentIds(chair.getTreatmentIds());
        
        model.addAttribute("dentalChairForm", form);
        model.addAttribute("chairId", id);
        model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
        
        return "clinic/chairs/form";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Validated @ModelAttribute DentalChairForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("chairId", id);
            model.addAttribute("treatments", treatmentTypeService.getTreatmentsByLoginId(userDetails.getUsername(), null));
            return "clinic/chairs/form";
        }
        
        dentalChairService.updateChair(id, userDetails.getUsername(), form);
        redirectAttributes.addFlashAttribute("successMessage", "チェア情報を更新しました。");
        return "redirect:/clinic/chairs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        dentalChairService.deleteChair(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "チェアを削除しました。");
        return "redirect:/clinic/chairs";
    }
}
