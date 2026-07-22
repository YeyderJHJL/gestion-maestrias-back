package com.claudecoders.masters.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Bulk enrollment request: enrolls multiple students into one course/semester")
public record EnrollmentBulkRequest(
		@NotEmpty List<UUID> studentIds,
		@NotNull UUID courseId,
		@NotNull Integer semesterId,
		@NotNull Integer stateId,
		@NotNull LocalDate enrollmentDate,
		UUID resolutionFileId,
		String observations
) {
}
