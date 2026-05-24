package com.claudecoders.masters.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Student response")
public record StudentResponse(
		UUID id,
		UUID userId,
		String email,
		String firstName,
		String lastName,
		Integer promotionId,
		String promotionName,
		String cui,
		String paymentCode,
		String phone,
		Instant createdAt,
		Instant updatedAt
) {
}
