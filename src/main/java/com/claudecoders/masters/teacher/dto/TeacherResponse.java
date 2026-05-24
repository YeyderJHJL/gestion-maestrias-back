package com.claudecoders.masters.teacher.dto;

import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Teacher response")
public record TeacherResponse(
		UUID id,
		UUID userId,
		String email,
		String firstName,
		String lastName,
		TeacherCategory category,
		String regime,
		AcademicDegree academicDegree,
		String specialty,
		TeacherType type,
		String phone,
		Instant createdAt,
		Instant updatedAt
) {
}
