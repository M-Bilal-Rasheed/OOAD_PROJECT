-- ============================================================
-- SPVSS Database Migration V3 — Scouting Reports
-- ============================================================
-- Table behind the ScoutingReport entity.
--
-- NOTE: the application runs with spring.jpa.hibernate.ddl-auto=update,
-- so Hibernate creates/extends this table automatically on start.
-- This script is the equivalent SQL for running manually in the
-- Supabase SQL Editor, and documents the schema. Safe to re-run.
--
-- Relationships:
--   scouting_reports.player_id -> players.id    (many reports per player)
--   scouting_reports.scout_id  -> app_users.id  (many reports per scout = owner)
-- When an Admin deletes a player, the application deletes that
-- player's reports first (PlayerService.deletePlayer).
-- ============================================================

CREATE TABLE IF NOT EXISTS public.scouting_reports (
    id                        BIGSERIAL PRIMARY KEY,
    player_id                 BIGINT        NOT NULL REFERENCES public.players(id),
    scout_id                  BIGINT        NOT NULL REFERENCES public.app_users(id),
    strengths                 TEXT          NOT NULL,
    weaknesses                TEXT          NOT NULL,
    potential                 VARCHAR(2000) NOT NULL,
    potential_rating          INTEGER       NOT NULL,
    performance_observations  TEXT          NOT NULL,
    recommendation            VARCHAR(32)   NOT NULL
        CHECK (recommendation IN ('STRONGLY_RECOMMEND', 'RECOMMEND', 'MONITOR', 'NOT_RECOMMENDED')),
    recommendation_summary    VARCHAR(2000) NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- An earlier version of this table existed in the project database with
-- extra nullable columns (scout_name, player_name, player_sport,
-- overall_recommendation). They are not used by the application and are
-- left in place. The statements below bring such a table up to date.
ALTER TABLE public.scouting_reports ADD COLUMN IF NOT EXISTS potential_rating       INTEGER;
ALTER TABLE public.scouting_reports ADD COLUMN IF NOT EXISTS recommendation         VARCHAR(32);
ALTER TABLE public.scouting_reports ADD COLUMN IF NOT EXISTS recommendation_summary VARCHAR(2000);
ALTER TABLE public.scouting_reports ALTER COLUMN potential TYPE VARCHAR(2000);

CREATE INDEX IF NOT EXISTS idx_scouting_reports_scout  ON public.scouting_reports(scout_id);
CREATE INDEX IF NOT EXISTS idx_scouting_reports_player ON public.scouting_reports(player_id);

-- Server-side access only (Spring Boot); RLS is not used, as for V2.
