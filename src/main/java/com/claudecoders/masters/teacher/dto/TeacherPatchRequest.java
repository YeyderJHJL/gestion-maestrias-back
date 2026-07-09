package com.claudecoders.masters.teacher.dto;

import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Teacher partial update request — only provided fields are applied")
public record TeacherPatchRequest(
		TeacherCategory category,
		@Size(max = 100) String regime,
		AcademicDegree academicDegree,
		@Size(max = 255) String specialty,
		TeacherType type,
		@Size(max = 20) String phone,
		@Size(max = 255) String university
) {
}
