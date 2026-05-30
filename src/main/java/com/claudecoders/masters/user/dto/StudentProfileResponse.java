package com.claudecoders.masters.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Student profile embedded in user profile")
public record StudentProfileResponse(
		UUID id,
		Integer promotionId,
		String promotionName,
		String cui,
		String paymentCode,
		String phone,
		Instant createdAt,
		Instant updatedAt
) {
}