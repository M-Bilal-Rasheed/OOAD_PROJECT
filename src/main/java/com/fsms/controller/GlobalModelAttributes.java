package com.fsms.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ============================================================
// Adds the logged-in user's role and name to every view model,
// so the shared navigation fragment can show the right links
// (Admin vs Scout) without each controller repeating this code.
// ============================================================
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentRole")
    public String currentRole(HttpSession session) {
        Object role = session.getAttribute("role");
        return role == null ? "" : role.toString();
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpSession session) {
        Object name = session.getAttribute("fullName");
        return name == null ? "" : name.toString();
    }
}
