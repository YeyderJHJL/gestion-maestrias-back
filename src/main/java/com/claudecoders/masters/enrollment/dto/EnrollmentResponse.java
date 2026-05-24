package com.claudecoders.masters.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Enrollment response")
public record EnrollmentResponse(
		UUID id,
		UUID studentId,
		String studentEmail,
		String studentName,
		UUID courseId,
		String courseCode,
		String courseName,
		Integer stateId,
		String stateCode,
		String stateName,
		LocalDate enrollmentDate,
		String resolutionUrl,
		String observations,
		Instant createdAt,
		Instant updatedAt
) {
}
