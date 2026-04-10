-- =========================
-- Test database cleanup
-- Order respects FK constraints (children first, parents last)
-- RESTART IDENTITY resets SERIAL sequences
-- =========================

TRUNCATE TABLE leveling_report RESTART IDENTITY CASCADE;
TRUNCATE TABLE job             RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_account    RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_auth       RESTART IDENTITY CASCADE;
TRUNCATE TABLE company         RESTART IDENTITY CASCADE;
