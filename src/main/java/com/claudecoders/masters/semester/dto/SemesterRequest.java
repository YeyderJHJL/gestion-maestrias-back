package com.claudecoders.masters.semester.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Semester create/update request")
public record SemesterRequest(
		@NotNull @Min(2001) Integer year,
		@NotBlank @Size(max = 50) String code
) {
}
