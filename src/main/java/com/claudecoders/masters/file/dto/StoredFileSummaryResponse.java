package com.claudecoders.masters.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stored file metadata without a signed download URL")
public record StoredFileSummaryResponse(
		UUID id,
		String originalName,
		String contentType,
		Long sizeBytes,
		Instant createdAt
) {
}
