package com.claudecoders.masters.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Course create/update request")
public record CourseRequest(
		@NotBlank @Size(max = 100) String code,
		@NotBlank @Size(max = 255) String name,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		String observations,
		UUID syllabusFileId
) {
}
