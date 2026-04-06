package com.societyshops.web;

import com.societyshops.service.PasswordResetService;
import com.societyshops.dto.RegisterRequest;
import com.societyshops.enums.Role;
import com.societyshops.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebAuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @GetMapping({"", "/login", "/register"})
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String phone,
                           @RequestParam String role,
                           RedirectAttributes ra) {
        try {
            RegisterRequest req = new RegisterRequest();
            req.setName(name);
            req.setEmail(email);
            req.setPassword(password);
            req.setPhone(phone);
            req.setRole(Role.valueOf(role));
            authService.register(req);
            ra.addFlashAttribute("success", "Registered successfully! Please login.");
            return "redirect:/web/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/login?registererror=true";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPage() { return "auth/forgot-password"; }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String email, RedirectAttributes ra) {
        passwordResetService.requestReset(email);
        ra.addFlashAttribute("success", "If that email is registered, a reset link has been sent.");
        return "redirect:/web/login";
    }

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String token, org.springframework.ui.Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam String token, @RequestParam String password, RedirectAttributes ra) {
        try {
            passwordResetService.resetPassword(token, password);
            ra.addFlashAttribute("success", "Password reset successfully! Please login.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/reset-password?token=" + token;
        }
        return "redirect:/web/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/web/admin/dashboard";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SHOPKEEPER"))) {
            return "redirect:/web/shopkeeper/dashboard";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESIDENT"))) {
            return "redirect:/web/resident/dashboard";
        }
        return "redirect:/web/login?error=true";
    }
}
