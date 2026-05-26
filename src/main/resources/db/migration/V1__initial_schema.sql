-- =============================================================================
-- Sistema de Gestión Académica de Maestrías - PostgreSQL Schema
-- =============================================================================
-- Convenciones:
--   - Tablas en plural, snake_case, atributos en inglés
--   - ENUMs en MAYÚSCULA y en inglés
--   - UUID v7 generado en aplicación por Hibernate (sin DEFAULT en BD)
--   - INTEGER / BIGINT GENERATED ALWAYS AS IDENTITY para PKs incrementales
--   - Soft delete vía deleted_at; unique se aplica como partial index
--   - Constraints CHECK e índices solo en casos críticos
-- =============================================================================


-- =============================================================================
-- 1. TIPOS ENUM
-- =============================================================================

CREATE TYPE user_role         AS ENUM ('ADMIN', 'TEACHER', 'STUDENT', 'COORDINATOR');
CREATE TYPE teacher_category  AS ENUM ('PRINCIPAL', 'ASSOCIATE', 'AUXILIARY');
CREATE TYPE teacher_type      AS ENUM ('INTERNAL', 'EXTERNAL');
CREATE TYPE academic_degree   AS ENUM ('MASTER', 'DOCTOR');
CREATE TYPE course_type       AS ENUM ('REGULAR', 'THESIS', 'TOPICS');
CREATE TYPE notification_type AS ENUM (
    'VOUCHER_UPLOADED',
    'VOUCHER_VALIDATED',
    'VOUCHER_OBSERVED',
    'VOUCHER_REJECTED',
    'GRADE_REGISTERED',
    'GRADE_MODIFIED',
    'ENROLLMENT_UPDATED'
);


-- =============================================================================
-- 2. TABLAS
-- =============================================================================

-- USERS -----------------------------------------------------------------------
-- first_name, last_name y dni centralizados aquí (no en teachers/students)
CREATE TABLE users (
    id          UUID         PRIMARY KEY,
    google_sub  VARCHAR(255),
    email       VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    dni         VARCHAR(20),
    role        user_role    NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);


-- TEACHERS --------------------------------------------------------------------
-- Sin first_name / last_name / dni — delegados a users
CREATE TABLE teachers (
    id               UUID             PRIMARY KEY,
    id_user          UUID             NOT NULL,
    category         teacher_category,
    regime           VARCHAR(100),
    academic_degree  academic_degree,
    specialty        VARCHAR(255),
    type             teacher_type     NOT NULL,
    phone            VARCHAR(20),
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);


-- STUDENTS --------------------------------------------------------------------
-- Sin first_name / last_name / dni — delegados a users
CREATE TABLE students (
    id            UUID         PRIMARY KEY,
    id_user       UUID         NOT NULL,
    id_promotion  INTEGER      NOT NULL,
    cui           VARCHAR(20)  NOT NULL,
    payment_code  VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);


-- PROGRAMS --------------------------------------------------------------------
CREATE TABLE programs (
    id          INTEGER      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);


-- PROMOTIONS ------------------------------------------------------------------
CREATE TABLE promotions (
    id          INTEGER      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_program  INTEGER      NOT NULL,
    name        VARCHAR(255) NOT NULL,
    period      VARCHAR(100),
    year        INTEGER      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);


-- COURSES ---------------------------------------------------------------------
CREATE TABLE courses (
    id            UUID         PRIMARY KEY,
    id_program    INTEGER      NOT NULL,
    id_promotion  INTEGER      NOT NULL,
    code          VARCHAR(100) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    type          course_type  NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    observations  TEXT,
    syllabus_url  TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);


-- ASSIGNMENTS -----------------------------------------------------------------
-- PK surrogate BIGINT — permite soft delete real y reasignación post-delete.
-- La unicidad (course, teacher) activa se garantiza con partial index.
CREATE TABLE assignments (
    id               BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_course        UUID        NOT NULL,
    id_teacher       UUID        NOT NULL,
    assignment_date  DATE        NOT NULL DEFAULT CURRENT_DATE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);


-- STATES ----------------------------------------------------------------------
CREATE TABLE states (
    id           INTEGER      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entity_type  VARCHAR(100) NOT NULL,
    code         VARCHAR(100) NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);


-- ENROLLMENTS -----------------------------------------------------------------
CREATE TABLE enrollments (
    id               UUID        PRIMARY KEY,
    id_student       UUID        NOT NULL,
    id_course        UUID        NOT NULL,
    id_state         INTEGER     NOT NULL,
    enrollment_date  DATE        NOT NULL DEFAULT CURRENT_DATE,
    resolution_url   TEXT,
    observations     TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);


-- GRADES ----------------------------------------------------------------------
CREATE TABLE grades (
    id             UUID     PRIMARY KEY,
    id_enrollment  UUID     NOT NULL,
    id_state       INTEGER  NOT NULL,
    value          SMALLINT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ
);


-- PENSIONS --------------------------------------------------------------------
CREATE TABLE pensions (
    id             INTEGER        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_promotion   INTEGER        NOT NULL,
    academic_year  INTEGER        NOT NULL,
    number         INTEGER        NOT NULL,
    concept        VARCHAR(255)   NOT NULL,
    amount         NUMERIC(10, 2) NOT NULL,
    due_date       DATE           NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ
);


-- PAYMENTS --------------------------------------------------------------------
CREATE TABLE payments (
    id           UUID     PRIMARY KEY,
    id_student   UUID     NOT NULL,
    id_pension   INTEGER  NOT NULL,
    id_state     INTEGER  NOT NULL,
    payment_date DATE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);


-- VOUCHERS --------------------------------------------------------------------
CREATE TABLE vouchers (
    id               UUID           PRIMARY KEY,
    id_payment       UUID           NOT NULL,
    id_state         INTEGER        NOT NULL,
    file_url         TEXT           NOT NULL,
    declared_amount  NUMERIC(10, 2) NOT NULL,
    observation      TEXT,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);


-- STORED_FILES ----------------------------------------------------------------
-- Append-only: metadata de archivos subidos a GCS. Sin soft delete.
-- La URL firmada se genera on-demand; object_key nunca se expone al cliente.
CREATE TABLE stored_files (
    id             UUID         PRIMARY KEY,
    original_name  VARCHAR(255) NOT NULL,
    content_type   VARCHAR(100) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    object_key     VARCHAR(500) NOT NULL,
    id_uploaded_by UUID         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


-- AUDIT_LOGS ------------------------------------------------------------------
-- Append-only. Sin field_name.
CREATE TABLE audit_logs (
    id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_user      UUID         NOT NULL,
    entity_type  VARCHAR(100) NOT NULL,
    id_entity    UUID         NOT NULL,
    action       VARCHAR(100) NOT NULL,
    old_value    JSONB,
    new_value    JSONB,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


-- NOTIFICATIONS ---------------------------------------------------------------
-- Append-only.
CREATE TABLE notifications (
    id           BIGINT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_user      UUID              NOT NULL,
    type         notification_type NOT NULL,
    message      TEXT              NOT NULL,
    entity_type  VARCHAR(100),
    id_entity    UUID,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);


-- =============================================================================
-- 3. FOREIGN KEYS
-- =============================================================================

ALTER TABLE teachers
    ADD CONSTRAINT fk_teachers_user     FOREIGN KEY (id_user) REFERENCES users(id);

ALTER TABLE students
    ADD CONSTRAINT fk_students_user      FOREIGN KEY (id_user)      REFERENCES users(id),
    ADD CONSTRAINT fk_students_promotion FOREIGN KEY (id_promotion) REFERENCES promotions(id);

ALTER TABLE promotions
    ADD CONSTRAINT fk_promotions_program FOREIGN KEY (id_program) REFERENCES programs(id);

ALTER TABLE courses
    ADD CONSTRAINT fk_courses_program    FOREIGN KEY (id_program)   REFERENCES programs(id),
    ADD CONSTRAINT fk_courses_promotion  FOREIGN KEY (id_promotion) REFERENCES promotions(id);

ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_course   FOREIGN KEY (id_course)  REFERENCES courses(id),
    ADD CONSTRAINT fk_assignments_teacher  FOREIGN KEY (id_teacher) REFERENCES teachers(id);

ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollments_student FOREIGN KEY (id_student) REFERENCES students(id),
    ADD CONSTRAINT fk_enrollments_course  FOREIGN KEY (id_course)  REFERENCES courses(id),
    ADD CONSTRAINT fk_enrollments_state   FOREIGN KEY (id_state)   REFERENCES states(id);

ALTER TABLE grades
    ADD CONSTRAINT fk_grades_enrollment FOREIGN KEY (id_enrollment) REFERENCES enrollments(id),
    ADD CONSTRAINT fk_grades_state      FOREIGN KEY (id_state)      REFERENCES states(id);

ALTER TABLE pensions
    ADD CONSTRAINT fk_pensions_promotion FOREIGN KEY (id_promotion) REFERENCES promotions(id);

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_student FOREIGN KEY (id_student) REFERENCES students(id),
    ADD CONSTRAINT fk_payments_pension FOREIGN KEY (id_pension) REFERENCES pensions(id),
    ADD CONSTRAINT fk_payments_state   FOREIGN KEY (id_state)   REFERENCES states(id);

ALTER TABLE vouchers
    ADD CONSTRAINT fk_vouchers_payment FOREIGN KEY (id_payment) REFERENCES payments(id),
    ADD CONSTRAINT fk_vouchers_state   FOREIGN KEY (id_state)   REFERENCES states(id);

ALTER TABLE stored_files
    ADD CONSTRAINT fk_stored_files_user FOREIGN KEY (id_uploaded_by) REFERENCES users(id);

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_user FOREIGN KEY (id_user) REFERENCES users(id);

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_user FOREIGN KEY (id_user) REFERENCES users(id);


-- =============================================================================
-- 4. UNIQUE INDEXES (partial — soft delete compatible)
-- =============================================================================

-- Autenticación en users
CREATE UNIQUE INDEX uq_users_google_sub  ON users(google_sub) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_email       ON users(email)      WHERE deleted_at IS NULL;
-- dni es nullable — se excluye null del índice único
CREATE UNIQUE INDEX uq_users_dni         ON users(dni)        WHERE deleted_at IS NULL AND dni IS NOT NULL;

-- Relación 1:1 teacher/student → user
CREATE UNIQUE INDEX uq_teachers_user     ON teachers(id_user) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_students_user     ON students(id_user) WHERE deleted_at IS NULL;

-- Unicidad de negocio en students
CREATE UNIQUE INDEX uq_students_cui           ON students(cui)          WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_students_payment_code  ON students(payment_code) WHERE deleted_at IS NULL;

-- Asignación activa única por (curso, docente)
CREATE UNIQUE INDEX uq_assignments_course_teacher
    ON assignments(id_course, id_teacher) WHERE deleted_at IS NULL;

-- Reglas de negocio
CREATE UNIQUE INDEX uq_states_entity_code
    ON states(entity_type, code) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_courses_promotion_code
    ON courses(id_promotion, code) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_pensions_promotion_year_number
    ON pensions(id_promotion, academic_year, number) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_payments_student_pension
    ON payments(id_student, id_pension) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_enrollments_student_course
    ON enrollments(id_student, id_course) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_grades_enrollment
    ON grades(id_enrollment) WHERE deleted_at IS NULL;


-- =============================================================================
-- 5. CHECK CONSTRAINTS
-- =============================================================================

ALTER TABLE grades
    ADD CONSTRAINT chk_grades_value CHECK (value >= 0 AND value <= 20);

ALTER TABLE pensions
    ADD CONSTRAINT chk_pensions_academic_year CHECK (academic_year IN (1, 2)),
    ADD CONSTRAINT chk_pensions_amount        CHECK (amount >= 0);


-- =============================================================================
-- 6. PERFORMANCE INDEXES
-- =============================================================================

CREATE INDEX idx_students_promotion   ON students(id_promotion)    WHERE deleted_at IS NULL;
CREATE INDEX idx_courses_promotion    ON courses(id_promotion)     WHERE deleted_at IS NULL;
CREATE INDEX idx_assignments_teacher  ON assignments(id_teacher)   WHERE deleted_at IS NULL;
CREATE INDEX idx_enrollments_course   ON enrollments(id_course)    WHERE deleted_at IS NULL;
CREATE INDEX idx_vouchers_payment     ON vouchers(id_payment)      WHERE deleted_at IS NULL;

CREATE INDEX idx_audit_logs_entity    ON audit_logs(entity_type, id_entity);

CREATE INDEX idx_notifications_user_unread
    ON notifications(id_user, created_at DESC)
    WHERE read_at IS NULL;
