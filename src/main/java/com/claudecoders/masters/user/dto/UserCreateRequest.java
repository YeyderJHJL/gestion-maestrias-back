package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.student.StudentStatus;
import com.claudecoders.masters.teacher.AcademicDegree;
import com.claudecoders.masters.teacher.TeacherCategory;
import com.claudecoders.masters.teacher.TeacherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

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
			@Size(max = 20) String phone,
			@Size(max = 255) String university
	) {
	}

	public record StudentProfileRequest(
			@NotNull @Min(2001) Integer yearPromotion,
			StudentStatus status,
			UUID reactualizationFileId,
			@NotBlank @Size(max = 20) String cui,
			@NotBlank @Size(max = 100) String paymentCode,
			@Size(max = 20) String phone
	) {
	}
}
