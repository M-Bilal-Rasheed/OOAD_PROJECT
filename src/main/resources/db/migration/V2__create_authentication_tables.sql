-- ============================================================
-- SPVSS Database Migration V2 — Authentication & Comparison
-- ============================================================
-- This script creates the tables needed for user authentication
-- and comparison history. Run this in your Supabase SQL Editor.
--
-- BEFORE RUNNING:
--   1. Go to https://supabase.com → Your Project → SQL Editor
--   2. Verify you are connected to your Supabase PostgreSQL database
--   3. You need the database owner/admin role to create tables
--
-- THIS MIGRATION:
--   • Creates app_users table (user accounts for login/signup)
--   • Creates comparisons table (saved comparisons per user)
--   • Creates comparison_players table (links comparisons to players)
--   • Preserves ALL existing data in players and shortlist tables
--   • Does NOT drop or recreate any existing tables
--
-- TABLE NAMES MATCH JPA ENTITY @Table(name="...") annotations:
--   • AppUser      → public.app_users
--   • Comparison   → public.comparisons
--   • ComparisonPlayer → public.comparison_players
--   • Player       → public.players
--   • ShortlistEntry → public.shortlist
--
-- ============================================================

-- 1. Create users table for authentication (AppUser entity)
CREATE TABLE IF NOT EXISTS public.app_users (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'SCOUT')),
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL
);

-- 2. Create comparisons table (Comparison entity)
CREATE TABLE IF NOT EXISTS public.comparisons (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
    title       VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL
);

-- 3. Create comparison_players join table (ComparisonPlayer entity)
-- Foreign key to players uses ON DELETE RESTRICT to preserve comparison history
-- even if a player is deleted from the players table.
CREATE TABLE IF NOT EXISTS public.comparison_players (
    id               BIGSERIAL PRIMARY KEY,
    comparison_id    BIGINT NOT NULL REFERENCES public.comparisons(id) ON DELETE CASCADE,
    player_id        BIGINT NOT NULL REFERENCES public.players(id) ON DELETE RESTRICT,
    created_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    UNIQUE (comparison_id, player_id)
);

-- 4. Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_app_users_email    ON public.app_users(email);
CREATE INDEX IF NOT EXISTS idx_comparisons_user   ON public.comparisons(user_id);
CREATE INDEX IF NOT EXISTS idx_comparison_player  ON public.comparison_players(player_id);

-- ============================================================
-- SECURITY NOTE:
--
-- This application uses SERVER-SIDE database access via Spring Boot.
-- All database operations go through the application server (Java/Spring),
-- NOT directly from the browser/client.
--
-- • RLS (Row Level Security) is NOT required — no client-side DB access
-- • DO NOT enable RLS on these tables — it would block your application
-- • Your Supabase connection string (DB_URL) contains credentials that
--   should NEVER be exposed to the browser. Spring Boot keeps them server-side.
--
-- ============================================================

-- ============================================================
-- HOW TO RUN THIS SCRIPT IN SUPABASE:
--
-- Step 1: Go to https://supabase.com → Your Project → SQL Editor
--
-- Step 2: Click "New Query"
--
-- Step 3: Copy and paste the ENTIRE contents of this file
--         (from "-- ============================================================" to the end)
--
-- Step 4: Click "Run"
--
-- Step 5: Expected output: "Command executed successfully" with no errors
--
-- Step 6: Verify tables were created by running:
--         SELECT table_name FROM information_schema.tables
--         WHERE table_schema = 'public' AND table_name IN
--         ('app_users', 'comparisons', 'comparison_players', 'players', 'shortlist')
--         ORDER BY table_name;
--
-- AFTER RUNNING:
--   • public.app_users      → NEW: user accounts (email, BCrypt hash, role)
--   • public.comparisons    → NEW: saved comparisons per user
--   • public.comparison_players → NEW: comparison-player associations
--   • public.players        → UNTOUCHED (your existing player records preserved)
--   • public.shortlist      → UNTOUCHED (your existing shortlist entries preserved)
-- ============================================================
