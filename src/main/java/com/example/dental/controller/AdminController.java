package com.example.dental.controller;

import com.example.dental.form.AdminEditForm;
import com.example.dental.form.ClinicEditForm;
import com.example.dental.form.ClinicRegistrationForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 管理者用コントローラー
 * - ログイン画面 (GET /admin/login)
 * - ダッシュボード (GET /admin/dashboard)
 * - ユーザー名/パスワード編集 (GET/POST /admin/edit)
 * - 医院登録 (GET/POST /admin/clinics/register)
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final com.example.dental.service.AdminService adminService;
    private final com.example.dental.service.DentalClinicService dentalClinicService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(com.example.dental.service.AdminService adminService,
            com.example.dental.service.DentalClinicService dentalClinicService,
            BCryptPasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.dentalClinicService = dentalClinicService;
        this.passwordEncoder = passwordEncoder;
    }

    // ─────────────────────────────────────────────────
    // ログイン画面
    // ─────────────────────────────────────────────────

    /**
     * ログイン画面を表示する。
     * Spring Security の formLogin 設定によりログイン処理自体は自動的に行われる。
     */
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // ─────────────────────────────────────────────────
    // ダッシュボード
    // ─────────────────────────────────────────────────

    /**
     * ログイン成功後に表示するダッシュボード。
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());
        model.addAttribute("admin", admin);
        return "admin/dashboard";
    }

    // ─────────────────────────────────────────────────
    // ユーザー名 / パスワード編集
    // ─────────────────────────────────────────────────

    /**
     * 編集フォームを表示する。
     */
    @GetMapping("/edit")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        AdminEditForm form = new AdminEditForm();
        form.setName(admin.getName());

        model.addAttribute("admin", admin);
        model.addAttribute("adminEditForm", form);
        return "admin/edit";
    }

    /**
     * 編集内容を保存する。
     * - 現在のパスワードで本人確認を行う
     * - 名前を更新する
     * - 新パスワードが入力されている場合のみパスワードを更新する
     */
    @PostMapping("/edit")
    public String editSubmit(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("adminEditForm") AdminEditForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        // バリデーションエラー
        if (bindingResult.hasErrors()) {
            model.addAttribute("admin", admin);
            return "admin/edit";
        }

        // 現在のパスワード確認
        if (!passwordEncoder.matches(form.getCurrentPassword(), admin.getPassword())) {
            model.addAttribute("admin", admin);
            model.addAttribute("currentPasswordError", "現在のパスワードが正しくありません");
            return "admin/edit";
        }

        // 新パスワードの一致確認（入力がある場合のみ）
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (!form.getNewPassword().equals(form.getConfirmPassword())) {
                model.addAttribute("admin", admin);
                model.addAttribute("confirmPasswordError", "新しいパスワードが一致しません");
                return "admin/edit";
            }
        }

        // プロフィール更新
        adminService.updateAdminProfile(userDetails.getUsername(), form);

        redirectAttributes.addFlashAttribute("successMessage", "プロフィールを更新しました");
        return "redirect:/admin/dashboard";
    }

    // ─────────────────────────────────────────────────
    // 医院アカウント管理
    // ─────────────────────────────────────────────────

    /**
     * 医院アカウント一覧を表示・検索する。
     */
    @GetMapping("/clinics")
    public String listClinics(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "contractStatus", required = false) com.example.dental.enums.ContractStatusName contractStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10);
        org.springframework.data.domain.Page<com.example.dental.dto.DentalClinicDto> clinicPage =
                dentalClinicService.getClinicsPage(name, contractStatus, pageable);

        model.addAttribute("admin", admin);
        model.addAttribute("clinics", clinicPage.getContent());
        model.addAttribute("page", clinicPage);
        model.addAttribute("searchName", name);
        model.addAttribute("searchStatus", contractStatus);
        model.addAttribute("statuses", com.example.dental.enums.ContractStatusName.values());

        return "admin/clinics/list";
    }    // ─────────────────────────────────────────────────
    // 医院アカウント登録
    // ─────────────────────────────────────────────────

    /**
     * 医院登録画面を表示する。
     */
    @GetMapping("/clinics/register")
    public String registerClinicForm(@AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        model.addAttribute("admin", admin);
        model.addAttribute("clinicRegistrationForm", new ClinicRegistrationForm());
        model.addAttribute("statuses", com.example.dental.enums.ContractStatusName.values());
        return "admin/clinics/register";
    }

    /**
     * 医院を登録する。
     */
    @PostMapping("/clinics/register")
    public String registerClinicSubmit(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("clinicRegistrationForm") ClinicRegistrationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        // ログインIDの重複チェック
        if (dentalClinicService.existsByLoginId(form.getLoginId())) {
            bindingResult.rejectValue("loginId", "error.loginId", "このログインIDはすでに登録されています");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("admin", admin);
            model.addAttribute("statuses", com.example.dental.enums.ContractStatusName.values());
            return "admin/clinics/register";
        }

        dentalClinicService.registerClinic(form);

        redirectAttributes.addFlashAttribute("successMessage", "新しい医院「" + form.getName() + "」を登録しました");
        return "redirect:/admin/dashboard";
    }

    // ─────────────────────────────────────────────────
    // 医院アカウント編集
    // ─────────────────────────────────────────────────

    /**
     * 医院編集画面を表示する。
     */
    @GetMapping("/clinics/{id}/edit")
    public String editClinicForm(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        com.example.dental.dto.DentalClinicDto clinic = dentalClinicService.getClinicById(id);

        ClinicEditForm form = new ClinicEditForm();
        form.setName(clinic.getName());
        form.setAddress(clinic.getAddress());
        form.setMail(clinic.getMail());
        form.setContractStatus(clinic.getContractStatus() != null ? clinic.getContractStatus().getStatusName() : null);

        model.addAttribute("admin", admin);
        model.addAttribute("clinic", clinic);
        model.addAttribute("clinicEditForm", form);
        model.addAttribute("statuses", com.example.dental.enums.ContractStatusName.values());
        return "admin/clinics/edit";
    }

    /**
     * 医院情報を更新する。
     */
    @PostMapping("/clinics/{id}/edit")
    public String editClinicSubmit(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("clinicEditForm") ClinicEditForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        com.example.dental.dto.AdminDto admin = adminService.getAdminByLoginId(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            com.example.dental.dto.DentalClinicDto clinic = dentalClinicService.getClinicById(id);
            model.addAttribute("admin", admin);
            model.addAttribute("clinic", clinic);
            model.addAttribute("statuses", com.example.dental.enums.ContractStatusName.values());
            return "admin/clinics/edit";
        }

        dentalClinicService.updateClinic(id, form);

        redirectAttributes.addFlashAttribute("successMessage", "医院「" + form.getName() + "」の情報を更新しました");
        return "redirect:/admin/dashboard";
    }
}
