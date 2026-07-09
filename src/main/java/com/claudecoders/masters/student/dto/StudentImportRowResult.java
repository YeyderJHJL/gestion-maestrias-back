package com.claudecoders.masters.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Result of processing a single Excel row during student import")
public record StudentImportRowResult(
		int rowNumber,
		String email,
		String dni,
		String cui,
		Integer yearPromotion,
		Status status,
		UUID studentId,
		List<String> observations
) {

	public enum Status {
		IMPORTED,
		REJECTED
	}
}
