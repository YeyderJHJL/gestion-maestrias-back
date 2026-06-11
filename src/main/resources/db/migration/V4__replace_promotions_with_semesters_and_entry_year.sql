CREATE TYPE student_status AS ENUM ('REGULAR', 'REACTUALIZATION');

CREATE TABLE semesters (
    id          INTEGER      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    year        INTEGER      NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT chk_semesters_year CHECK (year > 2000)
);

CREATE UNIQUE INDEX uq_semesters_year_code
    ON semesters(year, code) WHERE deleted_at IS NULL;

CREATE INDEX idx_semesters_year
    ON semesters(year) WHERE deleted_at IS NULL;

ALTER TABLE audit_logs
    ALTER COLUMN id_entity TYPE VARCHAR(100) USING id_entity::text;

INSERT INTO semesters(year, code)
SELECT EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER, 'MIGRADO'
WHERE EXISTS (SELECT 1 FROM assignments)
   OR EXISTS (SELECT 1 FROM enrollments);

CREATE TABLE programs_new (
    id             INTEGER      NOT NULL,
    name           VARCHAR(255) NOT NULL,
    pension_count  INTEGER      NOT NULL DEFAULT 14,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ
);

INSERT INTO programs_new (
    id,
    name,
    pension_count,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    id,
    name,
    14,
    created_at,
    updated_at,
    deleted_at
FROM programs;

CREATE TABLE teachers_new (
    id               UUID             NOT NULL,
    id_user          UUID             NOT NULL,
    category         teacher_category,
    regime           VARCHAR(100),
    academic_degree  academic_degree,
    specialty        VARCHAR(255),
    university       VARCHAR(255),
    type             teacher_type     NOT NULL,
    phone            VARCHAR(20),
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

INSERT INTO teachers_new (
    id,
    id_user,
    category,
    regime,
    academic_degree,
    specialty,
    university,
    type,
    phone,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    id,
    id_user,
    category,
    regime,
    academic_degree,
    specialty,
    university,
    type,
    phone,
    created_at,
    updated_at,
    deleted_at
FROM teachers;

CREATE TABLE students_new (
    id                       UUID           NOT NULL,
    id_user                  UUID           NOT NULL,
    year_promotion           INTEGER        NOT NULL,
    status                   student_status NOT NULL DEFAULT 'REGULAR',
    id_reactualization_file  UUID,
    cui                      VARCHAR(20)    NOT NULL,
    payment_code             VARCHAR(100)   NOT NULL,
    phone                    VARCHAR(20),
    created_at               TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at               TIMESTAMPTZ
);

INSERT INTO students_new (
    id,
    id_user,
    year_promotion,
    status,
    id_reactualization_file,
    cui,
    payment_code,
    phone,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    s.id,
    s.id_user,
    p.year,
    'REGULAR'::student_status,
    NULL,
    s.cui,
    s.payment_code,
    s.phone,
    s.created_at,
    s.updated_at,
    s.deleted_at
FROM students s
JOIN promotions p ON p.id = s.id_promotion;

CREATE TABLE courses_new (
    id                UUID         NOT NULL,
    code              VARCHAR(100) NOT NULL,
    name              VARCHAR(255) NOT NULL,
    type              course_type  NOT NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    observations      TEXT,
    id_syllabus_file  UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);

INSERT INTO courses_new (
    id,
    code,
    name,
    type,
    start_date,
    end_date,
    observations,
    id_syllabus_file,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    id,
    code,
    name,
    type,
    start_date,
    end_date,
    observations,
    id_syllabus_file,
    created_at,
    updated_at,
    deleted_at
FROM courses;

CREATE TABLE payments_new (
    id              UUID        NOT NULL,
    id_student      UUID        NOT NULL,
    payment_number  INTEGER     NOT NULL,
    payment_date    DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

INSERT INTO payments_new (
    id,
    id_student,
    payment_number,
    payment_date,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    pay.id,
    pay.id_student,
    pen.number,
    pay.payment_date,
    pay.created_at,
    pay.updated_at,
    pay.deleted_at
FROM payments pay
JOIN pensions pen ON pen.id = pay.id_pension;

CREATE TABLE enrollments_new (
    id                  UUID        NOT NULL,
    id_student          UUID        NOT NULL,
    id_course           UUID        NOT NULL,
    id_semester         INTEGER     NOT NULL,
    id_state            INTEGER     NOT NULL,
    enrollment_date     DATE        NOT NULL DEFAULT CURRENT_DATE,
    id_resolution_file  UUID,
    observations        TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

INSERT INTO enrollments_new (
    id,
    id_student,
    id_course,
    id_semester,
    id_state,
    enrollment_date,
    id_resolution_file,
    observations,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    id,
    id_student,
    id_course,
    (
        SELECT s.id
        FROM semesters s
        WHERE s.year = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER
          AND s.code = 'MIGRADO'
        ORDER BY s.id
        LIMIT 1
    ),
    id_state,
    enrollment_date,
    id_resolution_file,
    observations,
    created_at,
    updated_at,
    deleted_at
FROM enrollments;

CREATE TABLE vouchers_new (
    id          UUID        NOT NULL,
    id_payment  UUID        NOT NULL,
    id_state    INTEGER     NOT NULL,
    id_file     UUID        NOT NULL,
    observation TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);

INSERT INTO vouchers_new (
    id,
    id_payment,
    id_state,
    id_file,
    observation,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    id,
    id_payment,
    id_state,
    id_file,
    observation,
    created_at,
    updated_at,
    deleted_at
FROM vouchers;

CREATE TABLE assignments_new (
    id               BIGINT      NOT NULL,
    id_course        UUID        NOT NULL,
    id_teacher       UUID        NOT NULL,
    id_semester      INTEGER     NOT NULL,
    assignment_date  DATE        NOT NULL DEFAULT CURRENT_DATE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

INSERT INTO assignments_new (
    id,
    id_course,
    id_teacher,
    id_semester,
    assignment_date,
    created_at,
    updated_at,
    deleted_at
)
OVERRIDING SYSTEM VALUE
SELECT
    id,
    id_course,
    id_teacher,
    (
        SELECT s.id
        FROM semesters s
        WHERE s.year = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER
          AND s.code = 'MIGRADO'
        ORDER BY s.id
        LIMIT 1
    ),
    assignment_date,
    created_at,
    updated_at,
    deleted_at
FROM assignments;

ALTER TABLE grades DROP CONSTRAINT fk_grades_enrollment;

DROP TABLE assignments;
DROP TABLE vouchers;
DROP TABLE payments;
DROP TABLE enrollments;
DROP TABLE courses;
DROP TABLE students;
DROP TABLE pensions;
ALTER TABLE promotions DROP CONSTRAINT fk_promotions_program;
DROP TABLE promotions;
DROP TABLE teachers;
DROP TABLE programs;

ALTER TABLE programs_new    RENAME TO programs;
ALTER TABLE teachers_new    RENAME TO teachers;
ALTER TABLE students_new    RENAME TO students;
ALTER TABLE courses_new     RENAME TO courses;
ALTER TABLE payments_new    RENAME TO payments;
ALTER TABLE enrollments_new RENAME TO enrollments;
ALTER TABLE vouchers_new    RENAME TO vouchers;
ALTER TABLE assignments_new RENAME TO assignments;

ALTER TABLE programs
    ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;

ALTER TABLE assignments
    ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;

ALTER TABLE programs
    ADD CONSTRAINT programs_pkey PRIMARY KEY (id),
    ADD CONSTRAINT chk_programs_pension_count CHECK (pension_count > 0);

ALTER TABLE teachers
    ADD CONSTRAINT teachers_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_teachers_user FOREIGN KEY (id_user) REFERENCES users(id);

ALTER TABLE students
    ADD CONSTRAINT students_pkey PRIMARY KEY (id),
    ADD CONSTRAINT chk_students_year_promotion CHECK (year_promotion > 2000),
    ADD CONSTRAINT fk_students_user FOREIGN KEY (id_user) REFERENCES users(id),
    ADD CONSTRAINT fk_students_reactualization_file
        FOREIGN KEY (id_reactualization_file) REFERENCES stored_files(id);

ALTER TABLE courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_courses_syllabus_file FOREIGN KEY (id_syllabus_file) REFERENCES stored_files(id);

ALTER TABLE payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id),
    ADD CONSTRAINT chk_payments_payment_number CHECK (payment_number > 0),
    ADD CONSTRAINT fk_payments_student FOREIGN KEY (id_student) REFERENCES students(id);

ALTER TABLE enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_enrollments_student FOREIGN KEY (id_student) REFERENCES students(id),
    ADD CONSTRAINT fk_enrollments_course FOREIGN KEY (id_course) REFERENCES courses(id),
    ADD CONSTRAINT fk_enrollments_semester FOREIGN KEY (id_semester) REFERENCES semesters(id),
    ADD CONSTRAINT fk_enrollments_state FOREIGN KEY (id_state) REFERENCES states(id),
    ADD CONSTRAINT fk_enrollments_resolution_file
        FOREIGN KEY (id_resolution_file) REFERENCES stored_files(id);

ALTER TABLE vouchers
    ADD CONSTRAINT vouchers_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_vouchers_payment FOREIGN KEY (id_payment) REFERENCES payments(id),
    ADD CONSTRAINT fk_vouchers_state FOREIGN KEY (id_state) REFERENCES states(id),
    ADD CONSTRAINT fk_vouchers_file FOREIGN KEY (id_file) REFERENCES stored_files(id);

ALTER TABLE assignments
    ADD CONSTRAINT assignments_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_assignments_course FOREIGN KEY (id_course) REFERENCES courses(id),
    ADD CONSTRAINT fk_assignments_teacher FOREIGN KEY (id_teacher) REFERENCES teachers(id),
    ADD CONSTRAINT fk_assignments_semester FOREIGN KEY (id_semester) REFERENCES semesters(id);

ALTER TABLE grades
    ADD CONSTRAINT fk_grades_enrollment FOREIGN KEY (id_enrollment) REFERENCES enrollments(id);

CREATE UNIQUE INDEX uq_teachers_user
    ON teachers(id_user) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_students_user
    ON students(id_user) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_students_cui
    ON students(cui) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_students_payment_code
    ON students(payment_code) WHERE deleted_at IS NULL;

CREATE INDEX idx_students_year_promotion
    ON students(year_promotion) WHERE deleted_at IS NULL;

CREATE INDEX idx_students_reactualization_file
    ON students(id_reactualization_file)
    WHERE id_reactualization_file IS NOT NULL;

CREATE UNIQUE INDEX uq_courses_code
    ON courses(code) WHERE deleted_at IS NULL;

CREATE INDEX idx_courses_syllabus_file
    ON courses(id_syllabus_file)
    WHERE id_syllabus_file IS NOT NULL;

CREATE UNIQUE INDEX uq_payments_student_number
    ON payments(id_student, payment_number) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_enrollments_student_course_semester
    ON enrollments(id_student, id_course, id_semester) WHERE deleted_at IS NULL;

CREATE INDEX idx_enrollments_course
    ON enrollments(id_course) WHERE deleted_at IS NULL;

CREATE INDEX idx_enrollments_semester
    ON enrollments(id_semester) WHERE deleted_at IS NULL;

CREATE INDEX idx_enrollments_resolution_file
    ON enrollments(id_resolution_file)
    WHERE id_resolution_file IS NOT NULL;

CREATE UNIQUE INDEX uq_assignments_course_teacher_semester
    ON assignments(id_course, id_teacher, id_semester) WHERE deleted_at IS NULL;

CREATE INDEX idx_assignments_teacher
    ON assignments(id_teacher) WHERE deleted_at IS NULL;

CREATE INDEX idx_assignments_semester
    ON assignments(id_semester) WHERE deleted_at IS NULL;

CREATE INDEX idx_vouchers_payment
    ON vouchers(id_payment) WHERE deleted_at IS NULL;

CREATE INDEX idx_vouchers_file
    ON vouchers(id_file);

SELECT setval(pg_get_serial_sequence('programs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL)
FROM programs;

SELECT setval(pg_get_serial_sequence('assignments', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL)
FROM assignments;
