CREATE TYPE file_purpose AS ENUM (
    'SYLLABUS',
    'PAYMENT_VOUCHER',
    'ENROLLMENT_RESOLUTION',
    'REACTUALIZATION'
);

ALTER TABLE stored_files
    ADD COLUMN purpose file_purpose;

UPDATE stored_files file
SET purpose = 'SYLLABUS'
WHERE EXISTS (
    SELECT 1
    FROM courses course
    WHERE course.id_syllabus_file = file.id
);

UPDATE stored_files file
SET purpose = 'ENROLLMENT_RESOLUTION'
WHERE purpose IS NULL
  AND EXISTS (
      SELECT 1
      FROM enrollments enrollment
      WHERE enrollment.id_resolution_file = file.id
  );

UPDATE stored_files file
SET purpose = 'REACTUALIZATION'
WHERE purpose IS NULL
  AND EXISTS (
      SELECT 1
      FROM students student
      WHERE student.id_reactualization_file = file.id
  );

UPDATE stored_files file
SET purpose = 'PAYMENT_VOUCHER'
WHERE purpose IS NULL
  AND EXISTS (
      SELECT 1
      FROM vouchers voucher
      WHERE voucher.id_file = file.id
  );

-- Legacy uploads that are not referenced yet cannot be inferred safely.
-- Keep them usable only through the least privileged student upload purpose.
UPDATE stored_files
SET purpose = 'PAYMENT_VOUCHER'
WHERE purpose IS NULL;

ALTER TABLE stored_files
    ALTER COLUMN purpose SET NOT NULL;
