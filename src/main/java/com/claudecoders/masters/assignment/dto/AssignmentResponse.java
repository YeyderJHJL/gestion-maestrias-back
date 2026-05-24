package com.claudecoders.masters.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Assignment response")
public record AssignmentResponse(
		Long id,
		UUID courseId,
		String courseCode,
		String courseName,
		UUID teacherId,
		String teacherEmail,
		String teacherName,
		LocalDate assignmentDate,
		Instant createdAt,
		Instant updatedAt
) {
}
