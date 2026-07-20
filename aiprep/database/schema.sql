-- =====================================================================
-- AI Interview Preparation Platform — PostgreSQL schema
-- =====================================================================
-- This mirrors the JPA entities in src/main/java/com/aiprep/interview/entity
-- exactly. You do NOT have to run this by hand — with the default
-- application.properties setting (spring.jpa.hibernate.ddl-auto=update),
-- Spring Boot/Hibernate will create these same tables automatically the
-- first time the app connects to an empty database.
--
-- Run this file yourself instead if you want:
--   - to see/version-control the real schema explicitly, or
--   - to create the schema ahead of time and then switch
--     spring.jpa.hibernate.ddl-auto to "validate" in production
--     (recommended once the app is live, so Hibernate never silently
--     alters your production schema).
--
-- Usage:
--   psql "postgresql://USER:PASSWORD@HOST:PORT/DBNAME" -f schema.sql
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255),
    email                   VARCHAR(255)  NOT NULL UNIQUE,
    password_hash           VARCHAR(255),                 -- NULL for Google-only accounts
    role                    VARCHAR(30)   NOT NULL DEFAULT 'USER',      -- USER | ADMIN
    auth_provider           VARCHAR(30)   NOT NULL DEFAULT 'LOCAL',     -- LOCAL | GOOGLE
    otp_code                VARCHAR(20),
    otp_expiry              TIMESTAMP,
    reset_token             VARCHAR(255),
    reset_token_expiry      TIMESTAMP,
    banned                  BOOLEAN       NOT NULL DEFAULT FALSE,
    subscription_tier       VARCHAR(30)   NOT NULL DEFAULT 'FREE',      -- FREE | PREMIUM
    subscription_expiry     TIMESTAMP,
    interviews_used_today   INTEGER       NOT NULL DEFAULT 0,
    usage_reset_date        DATE          NOT NULL DEFAULT CURRENT_DATE,
    created_at              TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- ---------------------------------------------------------------------
-- interviews
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interviews (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role                VARCHAR(255),
    experience_level    VARCHAR(255),
    interview_type      VARCHAR(50),                                    -- HR | TECHNICAL | SYSTEM_DESIGN
    status              VARCHAR(30)   NOT NULL DEFAULT 'IN_PROGRESS',   -- IN_PROGRESS | COMPLETED | ABANDONED
    overall_score       DOUBLE PRECISION,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_interviews_user_id ON interviews (user_id);

-- ---------------------------------------------------------------------
-- interview_questions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interview_questions (
    id              BIGSERIAL PRIMARY KEY,
    interview_id    BIGINT      NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    question_text   TEXT,
    order_index     INTEGER,
    difficulty      VARCHAR(20)                                          -- EASY | MEDIUM | HARD
);

CREATE INDEX IF NOT EXISTS idx_questions_interview_id ON interview_questions (interview_id);

-- ---------------------------------------------------------------------
-- interview_answers  (1:1 with interview_questions)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interview_answers (
    id                  BIGSERIAL PRIMARY KEY,
    question_id         BIGINT      NOT NULL UNIQUE REFERENCES interview_questions (id) ON DELETE CASCADE,
    answer_text         TEXT,
    was_voice_input     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------
-- ai_feedback  (1:1 with interview_answers)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_feedback (
    id                      BIGSERIAL PRIMARY KEY,
    answer_id               BIGINT      NOT NULL UNIQUE REFERENCES interview_answers (id) ON DELETE CASCADE,
    score_out_of_10         DOUBLE PRECISION,
    technical_correctness   DOUBLE PRECISION,
    confidence              DOUBLE PRECISION,
    communication           DOUBLE PRECISION,
    keyword_coverage        DOUBLE PRECISION,
    suggestions             TEXT,
    sample_answer           TEXT
);

-- ---------------------------------------------------------------------
-- resumes
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resumes (
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    original_filename           VARCHAR(255),
    extracted_text              TEXT,
    target_role                 VARCHAR(255),
    ats_score                   DOUBLE PRECISION,
    missing_keywords            TEXT,   -- comma-separated
    weak_points                 TEXT,
    improvement_suggestions     TEXT,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes (user_id);

-- ---------------------------------------------------------------------
-- coding_submissions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS coding_submissions (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_title       VARCHAR(255),
    language            TEXT,                          -- java | python | javascript | cpp
    source_code         TEXT,
    stdin_input         TEXT,
    execution_output    TEXT,
    execution_success   BOOLEAN     NOT NULL DEFAULT FALSE,
    ai_review           TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_coding_submissions_user_id ON coding_submissions (user_id);

-- ---------------------------------------------------------------------
-- roadmap_plans
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roadmap_plans (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    current_skills      TEXT,
    target_role         VARCHAR(255),
    target_company      VARCHAR(255),
    generated_plan      TEXT,                          -- markdown-formatted roadmap
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_roadmap_plans_user_id ON roadmap_plans (user_id);

-- ---------------------------------------------------------------------
-- payments
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    razorpay_order_id       VARCHAR(255),
    razorpay_payment_id     VARCHAR(255),
    amount_paise            BIGINT,
    status                  VARCHAR(30) NOT NULL DEFAULT 'CREATED',    -- CREATED | PAID | FAILED
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments (user_id);

COMMIT;

-- =====================================================================
-- OPTIONAL: promote a user to admin after they've registered once
-- through the app (do this after real registration, don't insert a
-- fake password hash by hand):
--
--   UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
-- =====================================================================
