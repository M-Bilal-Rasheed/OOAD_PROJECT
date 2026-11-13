package com.fsms.service;

import com.fsms.model.*;
import com.fsms.repository.AppUserRepository;
import com.fsms.repository.PlayerRepository;
import com.fsms.repository.ScoutingReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Ownership rules for scouting reports, tested with mocked repositories.
class ScoutingReportServiceTest {

    private ScoutingReportRepository reportRepo;
    private PlayerRepository playerRepo;
    private AppUserRepository userRepo;
    private ScoutingReportService service;

    private AppUser alice;
    private AppUser bob;
    private Player player;
    private ScoutingReport alicesReport;

    @BeforeEach
    void setUp() {
        reportRepo = mock(ScoutingReportRepository.class);
        playerRepo = mock(PlayerRepository.class);
        userRepo = mock(AppUserRepository.class);
        service = new ScoutingReportService(reportRepo, playerRepo, userRepo);

        alice = user(1L, "Alice");
        bob = user(2L, "Bob");
        player = new Player();
        player.setId(10L);
        player.setName("Test Player");
        player.setSport("Football");

        alicesReport = new ScoutingReport();
        alicesReport.setId(100L);
        alicesReport.setAuthor(alice);
        alicesReport.setPlayer(player);
        form().applyTo(alicesReport);

        when(reportRepo.findById(100L)).thenReturn(Optional.of(alicesReport));
        when(reportRepo.save(any(ScoutingReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepo.findById(2L)).thenReturn(Optional.of(bob));
        when(playerRepo.findById(10L)).thenReturn(Optional.of(player));
    }

    private static AppUser user(Long id, String name) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setFullName(name);
        u.setRole("SCOUT");
        return u;
    }

    private static ScoutingReportForm form() {
        ScoutingReportForm f = new ScoutingReportForm();
        f.setPlayerId(10L);
        f.setStrengths("  Quick first step  ");
        f.setWeaknesses("Weak left foot");
        f.setPotential("Could start in a top league");
        f.setPotentialRating(8);
        f.setPerformanceObservations("Pressed well for 90 minutes");
        f.setRecommendation(Recommendation.RECOMMEND);
        f.setRecommendationSummary("Sign if the fee is reasonable");
        return f;
    }

    @Test
    void createAssociatesPlayerAndAuthorAndTrimsText() {
        ScoutingReport r = service.createReport(2L, form());
        assertNotNull(r);
        assertSame(bob, r.getAuthor());
        assertSame(player, r.getPlayer());
        assertEquals("Quick first step", r.getStrengths());
        assertEquals(8, r.getPotentialRating());
        assertEquals(Recommendation.RECOMMEND, r.getRecommendation());
    }

    @Test
    void createFailsForUnknownPlayer() {
        ScoutingReportForm f = form();
        f.setPlayerId(999L);
        when(playerRepo.findById(999L)).thenReturn(Optional.empty());
        assertNull(service.createReport(1L, f));
        verify(reportRepo, never()).save(any());
    }

    @Test
    void ownerCanReadButOtherScoutCannot() {
        assertSame(alicesReport, service.findOwnedReport(100L, 1L));
        assertNull(service.findOwnedReport(100L, 2L));
        assertNull(service.findOwnedReport(100L, null));
    }

    @Test
    void otherScoutCannotUpdate() {
        ScoutingReportForm changed = form();
        changed.setStrengths("Bob was here");
        assertNull(service.updateReport(100L, 2L, changed));
        assertEquals("Quick first step", alicesReport.getStrengths());
        verify(reportRepo, never()).save(any());
    }

    @Test
    void ownerCanUpdate() {
        ScoutingReportForm changed = form();
        changed.setRecommendation(Recommendation.MONITOR);
        assertNotNull(service.updateReport(100L, 1L, changed));
        assertEquals(Recommendation.MONITOR, alicesReport.getRecommendation());
        assertSame(player, alicesReport.getPlayer(), "player of a report never changes");
    }

    @Test
    void otherScoutCannotDelete() {
        assertFalse(service.deleteReport(100L, 2L));
        verify(reportRepo, never()).delete(any());
    }

    @Test
    void ownerCanDelete() {
        assertTrue(service.deleteReport(100L, 1L));
        verify(reportRepo).delete(alicesReport);
    }

    @Test
    void missingReportBehavesLikeNotOwned() {
        when(reportRepo.findById(555L)).thenReturn(Optional.empty());
        assertNull(service.findOwnedReport(555L, 1L));
        assertFalse(service.deleteReport(555L, 1L));
    }
}
