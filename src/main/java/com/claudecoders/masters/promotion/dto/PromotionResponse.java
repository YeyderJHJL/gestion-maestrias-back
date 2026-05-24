package com.claudecoders.masters.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Promotion response")
public record PromotionResponse(
		Integer id,
		Integer programId,
		String programName,
		String name,
		String period,
		Integer year,
		Instant createdAt,
		Instant updatedAt
) {
}
