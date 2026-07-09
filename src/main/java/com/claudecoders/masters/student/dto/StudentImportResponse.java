package com.claudecoders.masters.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Summary returned after importing students from an Excel file")
public record StudentImportResponse(
		int totalRows,
		int imported,
		int rejected,
		List<StudentImportRowResult> results
) {
}
