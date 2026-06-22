package com.claudecoders.masters.student.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import com.claudecoders.masters.student.StudentStatus;
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
		String dni,
		Integer yearPromotion,
		StudentStatus status,
		StoredFileSummaryResponse reactualizationFile,
		String cui,
		String paymentCode,
		String phone,
		Instant createdAt,
		Instant updatedAt
) {
}
