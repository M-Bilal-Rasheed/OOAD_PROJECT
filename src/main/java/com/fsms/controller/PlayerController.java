package com.fsms.controller;

import com.fsms.model.Player;
import com.fsms.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

// ============================================================
// MVC Pattern: PlayerController handles all Admin routes.
// Calls PlayerService for business logic; returns Thymeleaf view names.
// ============================================================
@Controller
@RequestMapping("/admin")
public class PlayerController {

    private final PlayerService playerService;

    private static final List<String> SPORTS = Arrays.asList(
            "Football", "Cricket", "Basketball", "Tennis"
    );

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Check session has a logged-in ADMIN user
    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"))
               && session.getAttribute("userId") != null;
    }

    // Admin dashboard — shows summary statistics
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("totalPlayers",    playerService.getTotalPlayerCount());
        model.addAttribute("valuedPlayers",   playerService.getValuedPlayerCount());
        model.addAttribute("footballCount",   playerService.getCountBySport("Football"));
        model.addAttribute("cricketCount",    playerService.getCountBySport("Cricket"));
        model.addAttribute("basketballCount", playerService.getCountBySport("Basketball"));
        model.addAttribute("tennisCount",     playerService.getCountBySport("Tennis"));
        model.addAttribute("fullName",        session.getAttribute("fullName"));
        return "admin/dashboard";
    }

    // List all players
    @GetMapping("/players")
    public String listPlayers(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("players", playerService.getAllPlayers());
        return "admin/player-list";
    }

    // Show blank add form
    @GetMapping("/players/new")
    public String showAddForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("player", new Player());
        model.addAttribute("sports", SPORTS);
        model.addAttribute("isEdit", false);
        return "admin/player-form";
    }

    // Show pre-filled edit form
    @GetMapping("/players/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Player player = playerService.getPlayerById(id);
        if (player == null) return "redirect:/admin/players";
        model.addAttribute("player", player);
        model.addAttribute("sports", SPORTS);
        model.addAttribute("isEdit", true);
        return "admin/player-form";
    }

    // Save (add or update) a player
    @PostMapping("/players/save")
    public String savePlayer(@Valid @ModelAttribute Player player,
                             BindingResult bindingResult,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("sports", SPORTS);
            model.addAttribute("isEdit", player.getId() != null);
            return "admin/player-form";
        }

        if (player.getId() == null) {
            playerService.addPlayer(player);
            redirectAttributes.addFlashAttribute("successMessage", "Player added successfully.");
        } else {
            playerService.updatePlayer(player);
            redirectAttributes.addFlashAttribute("successMessage", "Player updated successfully.");
        }
        return "redirect:/admin/players";
    }

    // Delete a player
    @PostMapping("/players/delete/{id}")
    public String deletePlayer(@PathVariable Long id, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";
        playerService.deletePlayer(id);
        redirectAttributes.addFlashAttribute("successMessage", "Player deleted successfully.");
        return "redirect:/admin/players";
    }
}
