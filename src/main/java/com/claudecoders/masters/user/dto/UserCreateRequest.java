package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "User create request with optional role-specific profile")
public record UserCreateRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@Size(max = 20) String dni,
		@NotNull UserRole role,
		Boolean active,
		@Valid TeacherProfileRequest teacher,
		@Valid StudentProfileRequest student
) {

	public record TeacherProfileRequest(
			TeacherCategory category,
			@Size(max = 100) String regime,
			AcademicDegree academicDegree,
			@Size(max = 255) String specialty,
			@NotNull TeacherType type,
			@Size(max = 20) String phone
	) {
	}

	public record StudentProfileRequest(
			@NotNull Integer promotionId,
			@NotBlank @Size(max = 20) String cui,
			@NotBlank @Size(max = 100) String paymentCode,
			@Size(max = 20) String phone
	) {
	}
}
