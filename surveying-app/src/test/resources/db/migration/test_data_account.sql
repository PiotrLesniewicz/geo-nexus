-- =========================
-- Test data: Company
-- =========================

INSERT INTO company (name, nip, street, building_number, postal_code, city, country, active, register_at, blocked_at)
VALUES ('GeoSurvey Sp. z o.o.', '1234567890', 'ul. Miernicza', '12', '00-001', 'Warszawa', 'Polska', TRUE,
        '2020-01-15T08:00:00Z', NULL),
       ('Geodeta Plus S.A.', '0987654321', 'ul. Polna', '7A', '30-002', 'Kraków', 'Polska', TRUE,
        '2021-03-22T09:30:00Z', NULL),
       ('TerraMap Biuro Geodezji', '1122334455', 'ul. Pomiarowa', '3B', '50-003', 'Wrocław', 'Polska', FALSE,
        '2019-06-10T07:00:00Z', '2022-06-10T07:00:00Z');


-- =========================
-- Test data: User account
-- =========================

INSERT INTO user_account (company_id, email, name, surname, role, active, register_at, deleted_at)
VALUES (1, 'jan.kowalski@geosurvey.pl', 'Jan', 'Kowalski', 'ADMIN', TRUE, '2020-02-01T08:00:00Z', NULL),
       (1, 'anna.nowak@geosurvey.pl', 'Anna', 'Nowak', 'SURVEYOR', TRUE, '2020-02-05T08:00:00Z', NULL),
       (2, 'piotr.wisniewski@geodeta.pl', 'Piotr', 'Wiśniewski', 'SURVEYOR', FALSE, '2021-04-01T10:00:00Z', NULL),
       (3, 'ula.zawada@geodeta.pl', 'Ula', 'Zawada', 'ADMIN', FALSE, '2021-04-01T10:00:00Z', '2023-10-20T12:00:00Z');

-- =========================
-- Test data: User auth
-- =========================
-- Password for all users: Password123 (BCrypt encoded)

INSERT INTO user_auth (user_id, password_hash, password_changed_at, must_change, created_at)
VALUES (1, '$2a$10$I3IxWVl/ch4pLKDjIW85R.EEti2YTOZuhd30FnPonrA3wjMtNbLUW', '2020-02-01T08:00:00Z', FALSE, '2020-02-01T08:00:00Z'),
       (2, '$2a$10$I3IxWVl/ch4pLKDjIW85R.EEti2YTOZuhd30FnPonrA3wjMtNbLUW', '2020-02-05T08:00:00Z', FALSE, '2020-02-05T08:00:00Z'),
       (3, '$2a$10$I3IxWVl/ch4pLKDjIW85R.EEti2YTOZuhd30FnPonrA3wjMtNbLUW', '2021-04-01T10:00:00Z', FALSE, '2021-04-01T10:00:00Z'),
       (4, '$2a$10$I3IxWVl/ch4pLKDjIW85R.EEti2YTOZuhd30FnPonrA3wjMtNbLUW', '2021-04-01T10:00:00Z', FALSE, '2021-04-01T10:00:00Z');

-- =========================
-- Test data: Job
-- =========================

INSERT INTO job (job_identifier, street, building_number, postal_code, city, country,
                 status, description, created_at, company_id, user_id)
VALUES ('JOB-2024-001', 'ul. Polna', '5', '00-100', 'Warszawa', 'Polska',
        'OPEN', 'Niwelacja terenu - test', '2024-06-01T08:00:00Z', 1, 2);
