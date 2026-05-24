package com.claudecoders.masters.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Promotion create/update request")
public record PromotionRequest(
		@NotNull Integer programId,
		@NotBlank @Size(max = 255) String name,
		@Size(max = 100) String period,
		@NotNull @Min(1900) Integer year
) {
}
