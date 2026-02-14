-- =========================
-- Company table
-- =========================
CREATE TABLE company
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    nip        VARCHAR(13) UNIQUE,
    -- Address fields
    street             VARCHAR(150),
    building_number    VARCHAR(20),
    apartment_number   VARCHAR(20),
    postal_code        VARCHAR(20),
    city               VARCHAR(100),
    county             VARCHAR(100),
    voivodeship        VARCHAR(100),
    country            VARCHAR(100),

    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    register_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    blocked_at  TIMESTAMPTZ
);

-- =========================
-- User table
-- =========================
CREATE TABLE user_account
(
    id            BIGSERIAL PRIMARY KEY,
    company_id    BIGINT NOT NULL REFERENCES company (id),
    email         VARCHAR(255) NOT NULL UNIQUE,
    name          VARCHAR(100),
    surname       VARCHAR(100),
    role          VARCHAR(50)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    register_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

-- =========================
-- Password / Auth table
-- =========================
CREATE TABLE user_auth
(
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    password_hash        VARCHAR(255) NOT NULL,
    password_changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    must_change          BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_auth UNIQUE (user_id)
);

-- =========================
-- Leveling Job (Administration)
-- =========================
CREATE TABLE job
(
    id             BIGSERIAL PRIMARY KEY,
    job_identifier VARCHAR(100) NOT NULL,
    company_id     BIGINT NOT NULL REFERENCES company(id),
    user_id        BIGINT NOT NULL REFERENCES user_account(id),
    -- Address of the site/project
    street             VARCHAR(150),
    building_number    VARCHAR(20),
    apartment_number   VARCHAR(20),
    postal_code        VARCHAR(20),
    city               VARCHAR(100),
    county             VARCHAR(100),
    voivodeship        VARCHAR(100),
    country            VARCHAR(100),
    status             VARCHAR(50) NOT NULL,
    description    TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_job_per_company UNIQUE (job_identifier, company_id)
);

-- =========================
-- Leveling Report (Technical report)
-- =========================
CREATE TABLE leveling_report
(
    id                     BIGSERIAL PRIMARY KEY,
    job_id                 BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    leveling_type          VARCHAR(50)   NOT NULL,
    start_height           NUMERIC(10,4),
    end_height             NUMERIC(10,4),
    measured_difference    NUMERIC(10,4) NOT NULL,
    theoretical_difference NUMERIC(10,4) NOT NULL,
    misclosure             NUMERIC(10,4) NOT NULL,
    allowed_misclosure     NUMERIC(10,4) NOT NULL CHECK (allowed_misclosure >= 0),
    is_within_tolerance    BOOLEAN NOT NULL,
    sequence_distance      NUMERIC(10,4) CHECK (sequence_distance >= 0),
    stations               JSONB NOT NULL,
    observation_time       TIMESTAMPTZ NOT NULL,
    generated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================
-- Indexes
-- =========================
CREATE INDEX idx_report_job ON leveling_report (job_id);