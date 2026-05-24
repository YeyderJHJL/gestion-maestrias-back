package com.claudecoders.masters.assignment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	boolean existsByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherId);

	Optional<Assignment> findByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherId);
}
