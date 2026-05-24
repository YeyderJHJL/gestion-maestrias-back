package com.claudecoders.masters.user.dto;

import com.claudecoders.masters.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "User create/update request")
public record UserRequest(
		@NotBlank @Size(max = 255) String googleSub,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@Size(max = 20) String dni,
		@NotNull UserRole role,
		Boolean active
) {
}
