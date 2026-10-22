package com.fsms.controller;

import com.fsms.model.AppUser;
import com.fsms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

// ============================================================
// AuthController handles login, signup, and logout.
// MVC Pattern: Controller layer — delegates to UserService.
// ============================================================
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ---- Home page now redirects to login ----
    @GetMapping("/")
    public String home(HttpSession session) {
        // If already logged in, go straight to their dashboard
        if (session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("role");
            if ("ADMIN".equals(role)) return "redirect:/admin/dashboard";
            if ("SCOUT".equals(role)) return "redirect:/scout/dashboard";
        }
        return "redirect:/login";
    }

    // ---- SHOW LOGIN PAGE ----
    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) String logout,
                            @RequestParam(required = false) String error,
                            HttpSession session,
                            Model model) {
        // Already logged in — skip login page
        if (session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("role");
            if ("ADMIN".equals(role)) return "redirect:/admin/dashboard";
            if ("SCOUT".equals(role)) return "redirect:/scout/dashboard";
        }
        if (logout != null) model.addAttribute("logoutMessage", "You have been logged out.");
        if (error  != null) model.addAttribute("errorMessage",  "Invalid email or password.");
        return "auth/login";
    }

    // ---- HANDLE LOGIN FORM SUBMISSION ----
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        
        logger.info("[LOGIN] POST received for email: {}", email);

        AppUser user = userService.verifyLogin(email, password);

        if (user == null) {
            logger.warn("[LOGIN] Failed: Invalid credentials for {}", email);
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
            model.addAttribute("enteredEmail", email);
            return "auth/login";
        }

        logger.info("[LOGIN] Success: {} logged in as {}", email, user.getRole());

        // Store user info in session (preserves existing session-based role checks)
        session.setAttribute("userId",    user.getId());
        session.setAttribute("role",      user.getRole());
        session.setAttribute("scoutName", user.getFullName());  // used by Scout shortlist logic
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("fullName",  user.getFullName());

        // Also tell Spring Security this user is authenticated
        // so that URL-level protection (@anyRequest().authenticated()) passes
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Redirect to correct dashboard based on role
        if ("ADMIN".equals(user.getRole())) {
            logger.info("[LOGIN] Redirecting to admin dashboard");
            return "redirect:/admin/dashboard";
        }
        logger.info("[LOGIN] Redirecting to scout dashboard");
        return "redirect:/scout/dashboard";
    }

    // ---- SHOW SIGNUP PAGE ----
    @GetMapping("/signup")
    public String showSignup(HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) return "redirect:/";
        return "auth/signup";
    }

    // ---- HANDLE SIGNUP FORM SUBMISSION ----
    @PostMapping("/signup")
    public String processSignup(@RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        logger.info("[SIGNUP] POST received for email: {}", email);

        // Password confirmation check (client-side also does this, but always validate server-side)
        if (!password.equals(confirmPassword)) {
            logger.warn("[SIGNUP] Failed: Passwords don't match for {}", email);
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("enteredName",  fullName);
            model.addAttribute("enteredEmail", email);
            return "auth/signup";
        }

        // Delegate to service — returns null on success, error message on failure
        String error = userService.registerScout(fullName, email, password);

        if (error != null) {
            logger.warn("[SIGNUP] Failed for {}: {}", email, error);
            model.addAttribute("errorMessage", error);
            model.addAttribute("enteredName",  fullName);
            model.addAttribute("enteredEmail", email);
            return "auth/signup";
        }

        logger.info("[SIGNUP] Success: {} registered as SCOUT", email);
        redirectAttributes.addFlashAttribute("successMessage",
                "Account created successfully! Please log in.");
        return "redirect:/login";
    }

    // ---- LOGOUT ----
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        logger.info("[LOGOUT] User logged out");
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }
}
