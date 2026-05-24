package com.claudecoders.masters.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Enrollment create/update request")
public record EnrollmentRequest(
		@NotNull UUID studentId,
		@NotNull UUID courseId,
		@NotNull Integer stateId,
		@NotNull LocalDate enrollmentDate,
		String resolutionUrl,
		String observations
) {
}
