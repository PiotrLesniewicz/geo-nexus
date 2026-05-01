-- ============================================================
-- SEED DATA
-- Passwords:
--   ADMINs:    GeoAdmin1!
--   SURVEYORs: GeoSurvey1!
-- ============================================================

-- ── user_account ────────────────────────────────────────────
-- Company 1: GeoSurvey Sp. z o.o. (active)
-- Company 2: Geodeta Plus S.A. (active)
-- Company 3: TerraMap Biuro Geodezji (blocked)

INSERT INTO company (name, nip, street, building_number, postal_code, city, country, active, register_at, blocked_at)
VALUES ('GeoSurvey Sp. z o.o.', '1234567890', 'ul. Miernicza', '12', '00-001', 'Warszawa', 'Polska', TRUE,
        '2020-01-15T08:00:00Z', NULL),
       ('Geodeta Plus S.A.', '0987654321', 'ul. Polna', '7A', '30-002', 'Kraków', 'Polska', TRUE,
        '2021-03-22T09:30:00Z', NULL),
       ('TerraMap Biuro Geodezji', '1122334455', 'ul. Pomiarowa', '3B', '50-003', 'Wrocław', 'Polska', FALSE,
        '2019-06-10T07:00:00Z', '2022-06-10T07:00:00Z');

INSERT INTO user_account (company_id, email, name, surname, role, active, register_at, deleted_at)
VALUES
    -- Company 1 – GeoSurvey (active)
    (2, 'jan.kowalski@geosurvey.pl', 'Jan', 'Kowalski', 'ADMIN', TRUE, '2020-02-01T08:00:00Z', NULL),
    (2, 'anna.nowak@geosurvey.pl', 'Anna', 'Nowak', 'SURVEYOR', TRUE, '2020-02-05T08:00:00Z', NULL),
    (2, 'marek.zajac@geosurvey.pl', 'Marek', 'Zając', 'SURVEYOR', TRUE, '2020-03-10T08:00:00Z', NULL),
    (2, 'tomasz.baran@geosurvey.pl', 'Tomasz', 'Baran', 'SURVEYOR', FALSE, '2020-06-01T08:00:00Z', NULL),

    -- Company 2 – Geodeta Plus (active)
    (3, 'katarzyna.lis@geodeta.pl', 'Katarzyna', 'Lis', 'ADMIN', TRUE, '2021-03-25T09:00:00Z', NULL),
    (3, 'piotr.wisniewski@geodeta.pl', 'Piotr', 'Wiśniewski', 'SURVEYOR', FALSE, '2021-04-01T10:00:00Z', NULL),
    (3, 'monika.krol@geodeta.pl', 'Monika', 'Król', 'SURVEYOR', TRUE, '2021-05-15T10:00:00Z', NULL),

    -- Company 3 – TerraMap (blocked)
    (4, 'ula.zawada@terramap.pl', 'Ula', 'Zawada', 'ADMIN', FALSE, '2019-07-01T07:00:00Z', '2023-10-20T12:00:00Z'),
    (4, 'adam.wrobel@terramap.pl', 'Adam', 'Wróbel', 'SURVEYOR', FALSE, '2019-08-01T07:00:00Z', NULL);

-- ── user_auth ────────────────────────────────────────────────
-- ADMINs    → GeoAdmin1!   → $2a$10$LDfGCVAsxwNCfSFGvTz7PefiKz6peOSdnEa8J191MI0E.cjqZDtvW
-- SURVEYORs → GeoSurvey1! → $2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m

INSERT INTO user_auth (user_id, password_hash, password_changed_at, must_change, created_at)
VALUES
    -- Company 1
    (2, '$2a$10$LDfGCVAsxwNCfSFGvTz7PefiKz6peOSdnEa8J191MI0E.cjqZDtvW', '2020-02-01T08:00:00Z', FALSE,
     '2020-02-01T08:00:00Z'), -- ADMIN
    (3, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2020-02-05T08:00:00Z', FALSE,
     '2020-02-05T08:00:00Z'), -- SURVEYOR
    (4, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2020-03-10T08:00:00Z', FALSE,
     '2020-03-10T08:00:00Z'), -- SURVEYOR
    (5, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2020-06-01T08:00:00Z', FALSE,
     '2020-06-01T08:00:00Z'), -- SURVEYOR inactive

    -- Company 2
    (6, '$2a$10$LDfGCVAsxwNCfSFGvTz7PefiKz6peOSdnEa8J191MI0E.cjqZDtvW', '2021-03-25T09:00:00Z', FALSE,
     '2021-03-25T09:00:00Z'), -- ADMIN
    (7, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2021-04-01T10:00:00Z', FALSE,
     '2021-04-01T10:00:00Z'), -- SURVEYOR inactive
    (8, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2021-05-15T10:00:00Z', FALSE,
     '2021-05-15T10:00:00Z'), -- SURVEYOR

    -- Company 3 (blocked)
    (9, '$2a$10$LDfGCVAsxwNCfSFGvTz7PefiKz6peOSdnEa8J191MI0E.cjqZDtvW', '2019-07-01T07:00:00Z', FALSE,
     '2019-07-01T07:00:00Z'), -- ADMIN deleted
    (10, '$2a$10$urDPPf5dvJJXGaS833oJVuVrvrJKfbfSWSsPzJRhMH0Hh4TLg/1.m', '2019-08-01T07:00:00Z', FALSE,
     '2019-08-01T07:00:00Z');
-- SURVEYOR

-- ── job ──────────────────────────────────────────────────────

INSERT INTO job (job_identifier, street, building_number, postal_code, city, country,
                 status, description, created_at, company_id, user_id)
VALUES
    -- Company 1 – GeoSurvey
    ('JOB-2024-001', 'ul. Polna', '5', '00-100', 'Warszawa', 'Polska', 'OPEN',
     'Niwelacja terenu – działka inwestycyjna', '2024-06-01T08:00:00Z', 2, 3),
    ('JOB-2024-002', 'ul. Miernicza', '12A', '00-200', 'Warszawa', 'Polska', 'OPEN', 'Niwelacja sieci kanalizacyjnej',
     '2024-07-15T09:00:00Z', 2, 4),
    ('JOB-2024-003', 'ul. Kolejowa', '1', '01-001', 'Warszawa', 'Polska', 'CLOSED',
     'Niwelacja torowiska – odcinek próbny', '2024-03-10T07:30:00Z', 2, 3);
