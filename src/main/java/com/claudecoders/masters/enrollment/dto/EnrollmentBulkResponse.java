package com.claudecoders.masters.enrollment.dto;

import java.util.List;

public record EnrollmentBulkResponse(
		int totalRows,
		int enrolled,
		int rejected,
		List<EnrollmentBulkRowResult> results
) {
}
