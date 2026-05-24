package com.claudecoders.masters.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Grade response")
public record GradeResponse(
		UUID id,
		UUID enrollmentId,
		UUID studentId,
		String studentEmail,
		UUID courseId,
		String courseCode,
		String courseName,
		Integer stateId,
		String stateCode,
		String stateName,
		Short value,
		Instant createdAt,
		Instant updatedAt
) {
}
