package com.claudecoders.masters.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Assignment syllabus file request")
public record AssignmentSyllabusRequest(
		@NotNull UUID syllabusFileId
) {
}
