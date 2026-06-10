ALTER TABLE courses
    ADD COLUMN id_syllabus_file UUID;

ALTER TABLE enrollments
    ADD COLUMN id_resolution_file UUID;

ALTER TABLE vouchers
    ADD COLUMN id_file UUID NOT NULL;

ALTER TABLE courses
    ADD CONSTRAINT fk_courses_syllabus_file
    FOREIGN KEY (id_syllabus_file) REFERENCES stored_files(id);

ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollments_resolution_file
    FOREIGN KEY (id_resolution_file) REFERENCES stored_files(id);

ALTER TABLE vouchers
    ADD CONSTRAINT fk_vouchers_file
    FOREIGN KEY (id_file) REFERENCES stored_files(id);

CREATE INDEX idx_courses_syllabus_file
    ON courses(id_syllabus_file)
    WHERE id_syllabus_file IS NOT NULL;

CREATE INDEX idx_enrollments_resolution_file
    ON enrollments(id_resolution_file)
    WHERE id_resolution_file IS NOT NULL;

CREATE INDEX idx_vouchers_file
    ON vouchers(id_file);

ALTER TABLE courses
    DROP COLUMN syllabus_url;

ALTER TABLE enrollments
    DROP COLUMN resolution_url;

ALTER TABLE vouchers
    DROP COLUMN file_url;
