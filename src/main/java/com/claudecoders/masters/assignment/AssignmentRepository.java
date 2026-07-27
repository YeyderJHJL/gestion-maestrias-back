package com.claudecoders.masters.assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	boolean existsByCourse_IdAndTeacher_IdAndSemester_Id(UUID courseId, UUID teacherId, Integer semesterId);

	Optional<Assignment> findByCourse_IdAndTeacher_IdAndSemester_Id(UUID courseId, UUID teacherId, Integer semesterId);

	List<Assignment> findByCourse_IdOrderByAssignmentDateAsc(UUID courseId);

	List<Assignment> findByTeacher_IdOrderByAssignmentDateAsc(UUID teacherId);

	boolean existsByCourse_IdAndTeacher_User_Id(UUID courseId, UUID userId);
}
