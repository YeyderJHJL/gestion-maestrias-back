package com.claudecoders.masters.course.dto;

import com.claudecoders.masters.course.CourseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Course create/update request")
public record CourseRequest(
		@NotNull Integer programId,
		@NotNull Integer promotionId,
		@NotBlank @Size(max = 100) String code,
		@NotBlank @Size(max = 255) String name,
		@NotNull CourseType type,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		String observations,
		String syllabusUrl
) {
}
