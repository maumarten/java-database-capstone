package com.project.back_end.mvc;

import com.yourorg.clinic.security.TokenValidationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final TokenValidationService tokenValidationService;

    public DashboardController(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    /**
     * Admin dashboard (Thymeleaf)
     * GET /adminDashboard/{token}
     * - Valid token → render templates/admin/adminDashboard.html
     * - Invalid     → redirect to /
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {
        Map<String, String> errors = tokenValidationService.validateToken(token, "admin");
        if (errors == null || errors.isEmpty()) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    /**
     * Doctor dashboard (Thymeleaf)
     * GET /doctorDashboard/{token}
     * - Valid token → render templates/doctor/doctorDashboard.html
     * - Invalid     → redirect to /
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {
        Map<String, String> errors = tokenValidationService.validateToken(token, "doctor");
        if (errors == null || errors.isEmpty()) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}
