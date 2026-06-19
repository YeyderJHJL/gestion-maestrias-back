package com.claudecoders.masters.assignment.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
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
		Integer semesterId,
		Integer semesterYear,
		String semesterCode,
		LocalDate assignmentDate,
		StoredFileSummaryResponse syllabusFile,
		Instant createdAt,
		Instant updatedAt
) {
}
