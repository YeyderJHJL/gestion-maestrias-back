package com.claudecoders.masters.semester.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Semester response")
public record SemesterResponse(
		Integer id,
		Integer year,
		String code,
		Instant createdAt,
		Instant updatedAt
) {
}
