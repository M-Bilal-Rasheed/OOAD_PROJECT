package com.fsms.repository;

import com.fsms.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// MVC Pattern: Repository layer handles all database access for Player.
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Search players by name, sport, position, and team.
    // Empty string means "no filter on this field".
    @Query("SELECT p FROM Player p WHERE " +
           "(:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:sport = '' OR LOWER(p.sport) = LOWER(:sport)) AND " +
           "(:position = '' OR LOWER(p.position) LIKE LOWER(CONCAT('%', :position, '%'))) AND " +
           "(:club = '' OR LOWER(p.club) LIKE LOWER(CONCAT('%', :club, '%')))")
    List<Player> searchPlayers(@Param("name") String name,
                               @Param("sport") String sport,
                               @Param("position") String position,
                               @Param("club") String club);

    // All players of one sport (used by Performance Analytics)
    List<Player> findBySportIgnoreCase(String sport);
}
