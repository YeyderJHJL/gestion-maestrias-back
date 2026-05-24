package com.claudecoders.masters.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Program response")
public record ProgramResponse(
		Integer id,
		String name,
		Instant createdAt,
		Instant updatedAt
) {
}
