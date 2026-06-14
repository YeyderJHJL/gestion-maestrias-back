package com.claudecoders.masters.student.dto;

import com.claudecoders.masters.student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Change student academic status and/or toggle user account active flag")
public record StudentStatusRequest(
		StudentStatus status,
		Boolean active
) {
}
