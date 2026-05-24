package com.claudecoders.masters.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Assignment create/update request")
public record AssignmentRequest(
		@NotNull UUID courseId,
		@NotNull UUID teacherId,
		@NotNull LocalDate assignmentDate
) {
}
