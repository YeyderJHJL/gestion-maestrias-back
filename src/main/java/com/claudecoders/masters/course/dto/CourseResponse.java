package com.claudecoders.masters.course.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Course response")
public record CourseResponse(
		UUID id,
		String code,
		String name,
		LocalDate startDate,
		LocalDate endDate,
		String observations,
		StoredFileSummaryResponse syllabusFile,
		Instant createdAt,
		Instant updatedAt
) {
}
