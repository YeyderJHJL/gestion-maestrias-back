ALTER TABLE assignments
    ADD COLUMN id_syllabus_file UUID;

UPDATE assignments assignment
SET id_syllabus_file = course.id_syllabus_file
FROM courses course
WHERE course.id = assignment.id_course
  AND course.id_syllabus_file IS NOT NULL;

ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_syllabus_file
    FOREIGN KEY (id_syllabus_file) REFERENCES stored_files(id);

CREATE INDEX idx_assignments_syllabus_file
    ON assignments(id_syllabus_file)
    WHERE id_syllabus_file IS NOT NULL;

DROP INDEX IF EXISTS idx_courses_syllabus_file;

ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS fk_courses_syllabus_file,
    DROP COLUMN IF EXISTS id_syllabus_file;
