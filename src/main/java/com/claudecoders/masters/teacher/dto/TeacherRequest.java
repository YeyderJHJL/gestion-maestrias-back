package com.claudecoders.masters.teacher.dto;

import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Teacher create/update request")
public record TeacherRequest(
		@NotNull UUID userId,
		TeacherCategory category,
		@Size(max = 100) String regime,
		AcademicDegree academicDegree,
		@Size(max = 255) String specialty,
		@NotNull TeacherType type,
		@Size(max = 20) String phone
) {
}
