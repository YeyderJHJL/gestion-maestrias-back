package com.claudecoders.masters.student.dto;

import com.claudecoders.masters.student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Student bulk JSON import item (frontend pre-processed from Excel)")
public record StudentBulkRequest(
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(max = 20) String dni,
		@NotNull Integer yearPromotion,
		@NotBlank @Size(max = 20) String cui,
		@NotBlank @Size(max = 100) String paymentCode,
		@Size(max = 20) String phone,
		StudentStatus status,
		UUID reactualizationFileId
) {
}
