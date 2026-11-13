package com.fsms.service;

import com.fsms.model.AppUser;
import com.fsms.model.Player;
import com.fsms.model.ScoutingReport;
import com.fsms.model.ScoutingReportForm;
import com.fsms.repository.AppUserRepository;
import com.fsms.repository.PlayerRepository;
import com.fsms.repository.ScoutingReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// ============================================================
// ScoutingReportService — business rules for scouting reports.
//
// Ownership rule: every read, update and delete goes through
// findOwnedReport(), which returns the report ONLY when the
// requesting user is its author. A scout can therefore never see,
// change or delete another scout's report, even by typing the URL.
//
// MVC Pattern: Service layer.
// ============================================================
@Service
public class ScoutingReportService {

    private final ScoutingReportRepository reportRepository;
    private final PlayerRepository playerRepository;
    private final AppUserRepository userRepository;

    public ScoutingReportService(ScoutingReportRepository reportRepository,
                                 PlayerRepository playerRepository,
                                 AppUserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    // Create a new report. Returns null if the author or player does not exist.
    @Transactional
    public ScoutingReport createReport(Long authorId, ScoutingReportForm form) {
        if (authorId == null || form == null || form.getPlayerId() == null) return null;
        Optional<AppUser> author = userRepository.findById(authorId);
        Optional<Player> player = playerRepository.findById(form.getPlayerId());
        if (author.isEmpty() || player.isEmpty()) return null;

        ScoutingReport report = new ScoutingReport();
        report.setAuthor(author.get());
        report.setPlayer(player.get());
        form.applyTo(report);
        return reportRepository.save(report);
    }

    // The report, only if it exists AND belongs to this user; otherwise null
    public ScoutingReport findOwnedReport(Long reportId, Long userId) {
        if (reportId == null || userId == null) return null;
        Optional<ScoutingReport> found = reportRepository.findById(reportId);
        if (found.isPresent() && found.get().isOwnedBy(userId)) {
            return found.get();
        }
        return null;
    }

    // Update the editable fields. The player and author never change.
    @Transactional
    public ScoutingReport updateReport(Long reportId, Long userId, ScoutingReportForm form) {
        ScoutingReport report = findOwnedReport(reportId, userId);
        if (report == null || form == null) return null;
        form.applyTo(report);
        return reportRepository.save(report);
    }

    // Delete — returns false if not found or not owned by this user
    @Transactional
    public boolean deleteReport(Long reportId, Long userId) {
        ScoutingReport report = findOwnedReport(reportId, userId);
        if (report == null) return false;
        reportRepository.delete(report);
        return true;
    }

    public List<ScoutingReport> getReportsByAuthor(Long authorId) {
        return reportRepository.findByAuthor_IdOrderByUpdatedAtDesc(authorId);
    }

    public List<ScoutingReport> getReportsByAuthorForPlayer(Long authorId, Long playerId) {
        return reportRepository.findByAuthor_IdAndPlayer_IdOrderByUpdatedAtDesc(authorId, playerId);
    }

    public long countByAuthor(Long authorId) {
        return authorId == null ? 0 : reportRepository.countByAuthor_Id(authorId);
    }
}
