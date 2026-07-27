package com.claudecoders.masters.enrollment.dto;

import java.util.List;
import java.util.UUID;

public record EnrollmentBulkRowResult(
		int rowNumber,
		UUID studentId,
		Status status,
		UUID enrollmentId,
		List<String> observations
) {
	public enum Status {
		ENROLLED,
		REJECTED
	}
}
