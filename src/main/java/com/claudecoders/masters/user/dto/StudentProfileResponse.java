package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import com.claudecoders.masters.student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Student profile embedded in user profile")
public record StudentProfileResponse(
		UUID id,
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
