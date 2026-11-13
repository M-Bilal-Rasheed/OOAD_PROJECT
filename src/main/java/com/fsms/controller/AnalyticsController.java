package com.fsms.controller;

import com.fsms.analytics.SportSummary;
import com.fsms.service.PerformanceAnalyticsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ============================================================
// AnalyticsController — the Performance Analytics page.
// Shows sport-specific KPI cards, per-stat leaderboards and the
// performance-index ranking for ONE sport at a time.
// MVC Pattern: Controller layer.
// ============================================================
@Controller
@RequestMapping("/scout/analytics")
public class AnalyticsController {

    private final PerformanceAnalyticsService analyticsService;

    public AnalyticsController(PerformanceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    private boolean isScoutOrAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return session.getAttribute("userId") != null && ("SCOUT".equals(role) || "ADMIN".equals(role));
    }

    @GetMapping("")
    public String analytics(@RequestParam(defaultValue = "Football") String sport,
                            HttpSession session, Model model) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        SportSummary summary = analyticsService.summarizeSport(sport);
        if (summary == null) {
            summary = analyticsService.summarizeSport("Football");
        }
        model.addAttribute("summary", summary);
        model.addAttribute("sports", analyticsService.getSports());
        model.addAttribute("selectedSport", summary.getSport());
        return "scout/analytics";
    }
}
