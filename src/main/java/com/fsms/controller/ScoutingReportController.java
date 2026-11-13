package com.fsms.controller;

import com.fsms.model.Player;
import com.fsms.model.Recommendation;
import com.fsms.model.ScoutingReport;
import com.fsms.model.ScoutingReportForm;
import com.fsms.service.PlayerService;
import com.fsms.service.ScoutingReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

// ============================================================
// ScoutingReportController — create, view, edit and delete
// personal scouting reports.
//
// Every request uses the logged-in user's id from the session.
// The service only returns reports owned by that user, so another
// scout's report id behaves exactly like a missing report.
// MVC Pattern: Controller layer.
// ============================================================
@Controller
@RequestMapping("/scout/reports")
public class ScoutingReportController {

    private static final String NOT_FOUND = "Report not found, or it belongs to another scout.";

    private final ScoutingReportService reportService;
    private final PlayerService playerService;

    public ScoutingReportController(ScoutingReportService reportService, PlayerService playerService) {
        this.reportService = reportService;
        this.playerService = playerService;
    }

    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    private boolean isScoutOrAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return getUserId(session) != null && ("SCOUT".equals(role) || "ADMIN".equals(role));
    }

    // ---- LIST MY REPORTS (optional sport filter) ----
    @GetMapping("")
    public String list(@RequestParam(defaultValue = "") String sport,
                       HttpSession session, Model model) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        List<ScoutingReport> all = reportService.getReportsByAuthor(getUserId(session));
        List<ScoutingReport> shown = new ArrayList<>();
        for (ScoutingReport r : all) {
            if (sport.isEmpty() || sport.equalsIgnoreCase(r.getPlayer().getSport())) shown.add(r);
        }
        model.addAttribute("reports", shown);
        model.addAttribute("totalReports", all.size());
        model.addAttribute("selectedSport", sport);
        return "scout/reports/list";
    }

    // ---- NEW REPORT FORM ----
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long playerId,
                          HttpSession session, Model model) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        ScoutingReportForm form = new ScoutingReportForm();
        if (playerId != null && playerService.getPlayerById(playerId) != null) {
            form.setPlayerId(playerId);
        }
        populateForm(model, form, null);
        return "scout/reports/form";
    }

    // ---- CREATE ----
    @PostMapping("")
    public String create(@Valid @ModelAttribute("reportForm") ScoutingReportForm form,
                         BindingResult bindingResult,
                         HttpSession session, Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        if (form.getPlayerId() != null && playerService.getPlayerById(form.getPlayerId()) == null) {
            bindingResult.rejectValue("playerId", "notFound", "That player no longer exists");
        }
        if (bindingResult.hasErrors()) {
            populateForm(model, form, null);
            return "scout/reports/form";
        }

        ScoutingReport saved = reportService.createReport(getUserId(session), form);
        if (saved == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not save the report. Please log in again.");
            return "redirect:/scout/reports";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Scouting report saved.");
        return "redirect:/scout/reports/" + saved.getId();
    }

    // ---- VIEW ONE REPORT ----
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, HttpSession session, Model model,
                       RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        ScoutingReport report = reportService.findOwnedReport(id, getUserId(session));
        if (report == null) {
            redirectAttributes.addFlashAttribute("errorMessage", NOT_FOUND);
            return "redirect:/scout/reports";
        }
        model.addAttribute("report", report);
        return "scout/reports/view";
    }

    // ---- EDIT FORM ----
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model,
                           RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        ScoutingReport report = reportService.findOwnedReport(id, getUserId(session));
        if (report == null) {
            redirectAttributes.addFlashAttribute("errorMessage", NOT_FOUND);
            return "redirect:/scout/reports";
        }
        populateForm(model, ScoutingReportForm.fromReport(report), report);
        return "scout/reports/form";
    }

    // ---- UPDATE ----
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("reportForm") ScoutingReportForm form,
                         BindingResult bindingResult,
                         HttpSession session, Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        Long userId = getUserId(session);

        ScoutingReport existing = reportService.findOwnedReport(id, userId);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", NOT_FOUND);
            return "redirect:/scout/reports";
        }
        // The player of a report is fixed once it is created
        form.setPlayerId(existing.getPlayer().getId());
        if (bindingResult.hasErrors()) {
            populateForm(model, form, existing);
            return "scout/reports/form";
        }

        reportService.updateReport(id, userId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Scouting report updated.");
        return "redirect:/scout/reports/" + id;
    }

    // ---- DELETE ----
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) Long returnPlayerId,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        boolean deleted = reportService.deleteReport(id, getUserId(session));
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Scouting report deleted.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", NOT_FOUND);
        }
        if (deleted && returnPlayerId != null) return "redirect:/scout/players/" + returnPlayerId;
        return "redirect:/scout/reports";
    }

    private void populateForm(Model model, ScoutingReportForm form, ScoutingReport existing) {
        model.addAttribute("reportForm", form);
        model.addAttribute("existingReport", existing);
        model.addAttribute("isEdit", existing != null);
        model.addAttribute("recommendations", Recommendation.values());
        List<Player> players = new ArrayList<>(playerService.getAllPlayers());
        players.sort((a, b) -> {
            int bySport = String.valueOf(a.getSport()).compareToIgnoreCase(String.valueOf(b.getSport()));
            return bySport != 0 ? bySport : String.valueOf(a.getName()).compareToIgnoreCase(String.valueOf(b.getName()));
        });
        model.addAttribute("players", players);
        Player selected = form.getPlayerId() == null ? null : playerService.getPlayerById(form.getPlayerId());
        model.addAttribute("selectedPlayer", selected);
    }
}
