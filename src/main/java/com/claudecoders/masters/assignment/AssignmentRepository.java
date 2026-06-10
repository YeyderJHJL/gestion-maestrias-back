package com.claudecoders.masters.assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	boolean existsByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherId);

	Optional<Assignment> findByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherId);

	List<Assignment> findByCourse_IdOrderByAssignmentDateAsc(UUID courseId);

	List<Assignment> findByTeacher_IdOrderByAssignmentDateAsc(UUID teacherId);
}
