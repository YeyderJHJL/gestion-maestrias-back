package com.claudecoders.masters.notification.dto;

import com.claudecoders.masters.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NotificationRequest(

	@NotNull
	UUID userId,

	@NotNull
	NotificationType type,

	@NotBlank
	String message,

	String entityType,

	UUID entityId

) {
}