package com.example.dental.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        String redirectUrl = "/";
        String roleName = "トップページ";
        
        if (requestUri != null) {
            String uri = requestUri.toString();
            if (uri.startsWith("/admin/")) {
                redirectUrl = "/admin/dashboard";
                roleName = "管理者ページ";
            } else if (uri.startsWith("/clinic/")) {
                redirectUrl = "/clinic/dashboard";
                roleName = "医院ページ";
            } else if (uri.startsWith("/patient/")) {
                // /patient/{token}/... の形を想定
                String[] parts = uri.split("/");
                if (parts.length >= 3 && "patient".equals(parts[1])) {
                    String token = parts[2];
                    redirectUrl = "/patient/" + token + "/dashboard";
                    roleName = "マイページ";
                }
            }
        }
        
        model.addAttribute("redirectUrl", redirectUrl);
        model.addAttribute("roleName", roleName);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }
        
        return "error/500"; // デフォルト
    }
}
