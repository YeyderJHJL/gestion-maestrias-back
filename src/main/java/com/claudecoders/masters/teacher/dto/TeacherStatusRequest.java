package com.claudecoders.masters.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Change teacher status (toggles the underlying user's active flag)")
public record TeacherStatusRequest(
		@NotNull Boolean active
) {
}
