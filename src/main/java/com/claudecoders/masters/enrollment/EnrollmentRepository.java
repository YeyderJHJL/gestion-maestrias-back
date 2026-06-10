package com.claudecoders.masters.enrollment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
  List<Enrollment> findByCourse_IdOrderByEnrollmentDateAsc(UUID courseId);
}