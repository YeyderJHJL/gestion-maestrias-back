package com.claudecoders.masters.teacher.dto;

import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Teacher bulk create item")
public record TeacherBulkRequest(
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@NotBlank @Email @Size(max = 255) String email,
		@Size(max = 20) String dni,
		@NotNull TeacherType type,
		TeacherCategory category,
		AcademicDegree academicDegree,
		@Size(max = 255) String specialty,
		@Size(max = 100) String regime,
		@Size(max = 20) String phone,
		@Size(max = 255) String university
) {
}
