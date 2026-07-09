package com.claudecoders.masters.student.dto;

import com.claudecoders.masters.student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Student create/update request")
public record StudentRequest(
		@NotNull UUID userId,
		@NotNull @Min(2001) Integer yearPromotion,
		StudentStatus status,
		UUID reactualizationFileId,
		@NotBlank @Size(max = 20) String cui,
		@NotBlank @Size(max = 100) String paymentCode,
		@Size(max = 20) String phone
) {
}
