package com.claudecoders.masters.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Program create/update request")
public record ProgramRequest(
		@NotBlank @Size(max = 255) String name,
		@NotNull @Min(1) Integer pensionCount
) {
}
