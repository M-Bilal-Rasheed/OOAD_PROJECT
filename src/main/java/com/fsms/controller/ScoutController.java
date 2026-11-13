package com.fsms.controller;

import com.fsms.analytics.PlayerComparison;
import com.fsms.model.Player;
import com.fsms.service.PerformanceAnalyticsService;
import com.fsms.service.PlayerService;
import com.fsms.service.ScoutingReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

// ============================================================
// MVC Pattern: ScoutController handles all Scout routes.
// Calls PlayerService for business logic; returns Thymeleaf view names.
// ============================================================
@Controller
@RequestMapping("/scout")
public class ScoutController {

    private final PlayerService playerService;
    private final PerformanceAnalyticsService analyticsService;
    private final ScoutingReportService reportService;

    public ScoutController(PlayerService playerService,
                           PerformanceAnalyticsService analyticsService,
                           ScoutingReportService reportService) {
        this.playerService = playerService;
        this.analyticsService = analyticsService;
        this.reportService = reportService;
    }

    // Check session has a logged-in SCOUT or ADMIN user
    // (Admins are allowed to browse the scout views too)
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    private boolean isScoutOrAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return isLoggedIn(session) && ("SCOUT".equals(role) || "ADMIN".equals(role));
    }

    private String getScoutName(HttpSession session) {
        return (String) session.getAttribute("scoutName");
    }

    // Scout dashboard — shows summary statistics
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        model.addAttribute("scoutName",    session.getAttribute("fullName"));
        model.addAttribute("totalPlayers",  playerService.getTotalPlayerCount());
        model.addAttribute("valuedPlayers", playerService.getValuedPlayerCount());
        model.addAttribute("footballCount",   playerService.getCountBySport("Football"));
        model.addAttribute("cricketCount",    playerService.getCountBySport("Cricket"));
        model.addAttribute("basketballCount", playerService.getCountBySport("Basketball"));
        model.addAttribute("tennisCount",     playerService.getCountBySport("Tennis"));
        String scoutName = getScoutName(session);
        model.addAttribute("shortlistCount",
                scoutName != null ? playerService.getShortlist(scoutName).size() : 0);
        model.addAttribute("reportCount",
                reportService.countByAuthor((Long) session.getAttribute("userId")));
        return "scout/dashboard";
    }

    // Show player list with optional search/filter
    @GetMapping("/players")
    public String listPlayers(@RequestParam(defaultValue = "") String name,
                              @RequestParam(defaultValue = "") String sport,
                              @RequestParam(defaultValue = "") String position,
                              @RequestParam(defaultValue = "") String club,
                              Model model, HttpSession session) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        List<Player> players = playerService.searchPlayers(name, sport, position, club);
        model.addAttribute("players", players);
        model.addAttribute("searchName",     name);
        model.addAttribute("searchSport",    sport);
        model.addAttribute("searchPosition", position);
        model.addAttribute("searchClub",     club);

        if (players.isEmpty()) {
            model.addAttribute("noResults", true);
        }
        return "scout/player-list";
    }

    // Show a single player's full profile
    @GetMapping("/players/{id}")
    public String playerDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        Player player = playerService.getPlayerById(id);
        if (player == null) return "redirect:/scout/players";

        String scoutName = getScoutName(session);
        boolean alreadyShortlisted = scoutName != null && playerService.isOnShortlist(scoutName, id);

        model.addAttribute("player", player);
        model.addAttribute("alreadyShortlisted", alreadyShortlisted);
        // Performance Analytics: this player measured against the same sport
        model.addAttribute("insight", analyticsService.analysePlayer(player));
        // Scouting Reports: only the logged-in scout's own reports on this player
        model.addAttribute("myReports",
                reportService.getReportsByAuthorForPlayer((Long) session.getAttribute("userId"), id));
        return "scout/player-detail";
    }

    // Calculate and save a player's estimated value
    @PostMapping("/players/{id}/valuate")
    public String valuatePlayer(@PathVariable Long id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        Player player = playerService.calculateAndSaveValue(id);
        if (player != null) {
            String formatted = "£" + String.format("%,d", player.getEstimatedValue());
            redirectAttributes.addFlashAttribute("valuationMessage",
                    "Estimated value calculated: " + formatted + " (educational estimate only)");
        }
        return "redirect:/scout/players/" + id;
    }

    // Show the comparison selection form
    @GetMapping("/compare")
    public String compareForm(@RequestParam(required = false) Long player1Id,
                              Model model, HttpSession session) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        populateCompareForm(model, player1Id);
        return "scout/compare";
    }

    private void populateCompareForm(Model model, Long player1Id) {
        model.addAttribute("players", playerService.getAllPlayers());
        model.addAttribute("sports", analyticsService.getSports());
        model.addAttribute("selected1", player1Id);
    }

    // Show side-by-side comparison result
    @GetMapping("/compare/result")
    public String compareResult(@RequestParam Long player1Id,
                                @RequestParam Long player2Id,
                                Model model, HttpSession session) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        if (player1Id.equals(player2Id)) {
            populateCompareForm(model, player1Id);
            model.addAttribute("errorMessage", "Please select two different players to compare.");
            return "scout/compare";
        }

        List<Player> twoPlayers = playerService.comparePlayers(player1Id, player2Id);
        if (twoPlayers.size() < 2) {
            populateCompareForm(model, null);
            model.addAttribute("errorMessage", "One of the selected players no longer exists.");
            return "scout/compare";
        }

        // Performance Analytics rule: compare only players of the same sport
        PlayerComparison comparison = analyticsService.comparePlayers(twoPlayers.get(0), twoPlayers.get(1));
        if (comparison == null) {
            populateCompareForm(model, player1Id);
            model.addAttribute("errorMessage",
                    "Players can only be compared with players of the same sport. "
                    + twoPlayers.get(0).getName() + " plays " + twoPlayers.get(0).getSport()
                    + " and " + twoPlayers.get(1).getName() + " plays " + twoPlayers.get(1).getSport() + ".");
            return "scout/compare";
        }

        model.addAttribute("comparison", comparison);
        model.addAttribute("twoPlayers",  twoPlayers);
        model.addAttribute("player1Id",   player1Id);
        model.addAttribute("player2Id",   player2Id);
        return "scout/compare-result";
    }

    // Show the scout's shortlist
    @GetMapping("/shortlist")
    public String viewShortlist(Model model, HttpSession session) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        String scoutName = getScoutName(session);
        List<Player> shortlist = scoutName != null
                ? playerService.getShortlist(scoutName)
                : new ArrayList<>();

        model.addAttribute("shortlist",  shortlist);
        model.addAttribute("scoutName",  session.getAttribute("fullName"));

        List<Long> entryIds = new ArrayList<>();
        if (scoutName != null) {
            for (Player p : shortlist) {
                entryIds.add(playerService.getShortlistEntryId(scoutName, p.getId()));
            }
        }
        model.addAttribute("entryIds", entryIds);
        return "scout/shortlist";
    }

    // Add a player to the shortlist
    @PostMapping("/shortlist/add/{playerId}")
    public String addToShortlist(@PathVariable Long playerId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";

        String scoutName = getScoutName(session);
        if (scoutName == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not identify your account.");
            return "redirect:/scout/players/" + playerId;
        }
        boolean added = playerService.addToShortlist(scoutName, playerId);
        if (added) {
            redirectAttributes.addFlashAttribute("successMessage", "Player added to your shortlist.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "This player is already on your shortlist.");
        }
        return "redirect:/scout/players/" + playerId;
    }

    // Remove a player from the shortlist (player NOT deleted — Aggregation)
    @PostMapping("/shortlist/remove/{entryId}")
    public String removeFromShortlist(@PathVariable Long entryId, HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!isScoutOrAdmin(session)) return "redirect:/login";
        playerService.removeFromShortlist(entryId);
        redirectAttributes.addFlashAttribute("successMessage", "Player removed from your shortlist.");
        return "redirect:/scout/shortlist";
    }
}
