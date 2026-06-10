package com.claudecoders.masters.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "File metadata and a time-limited signed URL for download")
public record StoredFileResponse(
		UUID id,
		String originalName,
		String contentType,
		Long sizeBytes,
		UUID uploadedById,
		Instant createdAt,
		@Schema(description = "Pre-signed URL valid for the configured expiration time — use this to download the file")
		String downloadUrl
) {
}
