package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "User response")
public record UserResponse(
		UUID id,
		String googleSub,
		String email,
		String firstName,
		String lastName,
		String dni,
		UserRole role,
		Boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
