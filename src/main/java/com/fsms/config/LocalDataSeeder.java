package com.fsms.config;

import com.fsms.model.Player;
import com.fsms.model.PlayerStatistics;
import com.fsms.service.PlayerService;
import com.fsms.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// ============================================================
// LocalDataSeeder runs only with the "local" profile.
// It creates demo accounts and players so the application can
// be explored immediately on the H2 in-memory database.
// ============================================================
@Component
@Profile("local")
@Order(2)
public class LocalDataSeeder implements ApplicationRunner {

    private final UserService userService;
    private final PlayerService playerService;

    public LocalDataSeeder(UserService userService, PlayerService playerService) {
        this.userService = userService;
        this.playerService = playerService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.seedAdminIfNotExists("System Admin", "admin@example.com", "admin123");
        userService.registerScout("Demo Scout", "scout@example.com", "scout123");

        if (playerService.getTotalPlayerCount() == 0) {
            seedPlayers();
        }
    }

    private void seedPlayers() {
        addPlayer("Erling Haaland", 23, "Norway", "Football", "Striker", "Manchester City", 52, 9, 35, 94);
        addPlayer("Kevin De Bruyne", 32, "Belgium", "Football", "Midfielder", "Manchester City", 8, 28, 31, 78);
        addPlayer("Virgil van Dijk", 32, "Netherlands", "Football", "Defender", "Liverpool", 3, 2, 34, 85);

        addPlayer("Joe Root", 33, "England", "Cricket", "Batsman", "England", 1147, 5, 512, 31);
        addPlayer("Pat Cummins", 30, "Australia", "Cricket", "Bowler", "Australia", 289, 48, 233, 35);
        addPlayer("Rashid Khan", 25, "Afghanistan", "Cricket", "All-rounder", "Afghanistan", 412, 39, 301, 46);

        addPlayer("Nikola Jokic", 28, "Serbia", "Basketball", "Center", "Denver Nuggets", 2085, 703, 817, 583);
        addPlayer("Stephen Curry", 35, "USA", "Basketball", "Point Guard", "Golden State Warriors", 1956, 412, 302, 427);
        addPlayer("Luka Doncic", 24, "Slovenia", "Basketball", "Point Guard", "Dallas Mavericks", 2370, 686, 647, 487);

        addPlayer("Novak Djokovic", 36, "Serbia", "Tennis", "Right-handed", "Serbia", 56, 1, 412, 648);
        addPlayer("Carlos Alcaraz", 20, "Spain", "Tennis", "Right-handed", "Spain", 64, 2, 388, 625);
        addPlayer("Iga Swiatek", 22, "Poland", "Tennis", "Right-handed", "Poland", 61, 1, 210, 671);
    }

    private void addPlayer(String name, int age, String nationality, String sport,
                           String position, String club,
                           int stat1, int stat2, int stat3, int stat4) {
        Player player = new Player();
        player.setName(name);
        player.setAge(age);
        player.setNationality(nationality);
        player.setSport(sport);
        player.setPosition(position);
        player.setClub(club);
        player.setStatistics(new PlayerStatistics(stat1, stat2, stat3, stat4));
        Player saved = playerService.addPlayer(player);
        playerService.calculateAndSaveValue(saved.getId());
    }
}
