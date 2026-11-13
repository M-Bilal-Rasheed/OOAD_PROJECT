package com.fsms.controller;

import com.fsms.analytics.PlayerComparison;
import com.fsms.model.Comparison;
import com.fsms.model.Player;
import com.fsms.service.ComparisonService;
import com.fsms.service.PerformanceAnalyticsService;
import com.fsms.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// ============================================================
// ComparisonController handles saved comparison history.
// Only Scouts (and Admins if needed) can access comparisons.
// Each user can only see their own saved comparisons.
// MVC Pattern: Controller layer — delegates to ComparisonService.
// ============================================================
@Controller
@RequestMapping("/scout/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;
    private final PlayerService playerService;
    private final PerformanceAnalyticsService analyticsService;

    public ComparisonController(ComparisonService comparisonService,
                                PlayerService playerService,
                                PerformanceAnalyticsService analyticsService) {
        this.comparisonService = comparisonService;
        this.playerService = playerService;
        this.analyticsService = analyticsService;
    }

    // Helper — get userId from session, return null if not logged in
    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    // ---- SAVE A NEW COMPARISON ----
    // Called from the compare-result page when the user clicks "Save Comparison"
    @PostMapping("/save")
    public String saveComparison(@RequestParam Long player1Id,
                                 @RequestParam Long player2Id,
                                 @RequestParam(defaultValue = "") String title,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (!isLoggedIn(session)) return "redirect:/login";

        // Only same-sport comparisons can be saved
        if (!analyticsService.isSameSport(playerService.getPlayerById(player1Id),
                                          playerService.getPlayerById(player2Id))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only players of the same sport can be compared and saved.");
            return "redirect:/scout/compare";
        }

        Long userId = getUserId(session);
        comparisonService.saveComparison(userId, title, player1Id, player2Id);

        redirectAttributes.addFlashAttribute("successMessage",
                "Comparison saved successfully. View it in your comparison history.");
        return "redirect:/scout/compare/result?player1Id=" + player1Id + "&player2Id=" + player2Id;
    }

    // ---- VIEW COMPARISON HISTORY ----
    @GetMapping("")
    public String viewHistory(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";

        Long userId = getUserId(session);
        List<Comparison> comparisons = comparisonService.getComparisonsForUser(userId);
        model.addAttribute("comparisons", comparisons);
        return "scout/comparison-history";
    }

    // ---- REOPEN A SAVED COMPARISON ----
    @GetMapping("/{id}")
    public String viewSavedComparison(@PathVariable Long id,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) return "redirect:/login";

        Long userId = getUserId(session);
        Comparison comparison = comparisonService.getComparisonForUser(id, userId);

        if (comparison == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Comparison not found or you do not have permission to view it.");
            return "redirect:/scout/comparisons";
        }

        List<Player> players = comparisonService.getPlayersForComparison(comparison);

        if (players.size() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "One or more players in this comparison no longer exist.");
            return "redirect:/scout/comparisons";
        }

        model.addAttribute("savedComparison", comparison);
        model.addAttribute("twoPlayers", players);
        // Older saved comparisons may mix sports: analytics is shown only for same-sport pairs
        PlayerComparison analysis = analyticsService.comparePlayers(players.get(0), players.get(1));
        model.addAttribute("comparison", analysis);
        return "scout/compare-result";
    }

    // ---- DELETE A SAVED COMPARISON ----
    @PostMapping("/{id}/delete")
    public String deleteComparison(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) return "redirect:/login";

        Long userId = getUserId(session);
        boolean deleted = comparisonService.deleteComparison(id, userId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Comparison deleted.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete comparison — not found or not yours.");
        }
        return "redirect:/scout/comparisons";
    }
}
