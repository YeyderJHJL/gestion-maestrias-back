package com.claudecoders.masters.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Grade create/update request")
public record GradeRequest(
		@NotNull UUID enrollmentId,
		@NotNull Integer stateId,
		@NotNull @Min(0) @Max(20) Short value
) {
}
