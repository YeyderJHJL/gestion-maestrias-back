package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Teacher profile embedded in user profile")
public record TeacherProfileResponse(
		UUID id,
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