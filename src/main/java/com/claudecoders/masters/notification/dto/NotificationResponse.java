package com.claudecoders.masters.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

		Long id,

		UUID userId,

		String type,

		String typeLabel,

		String message,

		String entityType,

		UUID entityId,

	  boolean read,

		Instant readAt,

		Instant createdAt

) {
}