package com.claudecoders.masters.auditlog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
		UUID actorUserId,
		String entityType,
		UUID entityId,
		AuditAction action,
		Map<String, Object> oldValues,
		Map<String, Object> newValues
) {

	public AuditEvent {
		Objects.requireNonNull(actorUserId, "actorUserId is required");
		Objects.requireNonNull(entityType, "entityType is required");
		Objects.requireNonNull(entityId, "entityId is required");
		Objects.requireNonNull(action, "action is required");
		oldValues = immutableCopy(oldValues);
		newValues = immutableCopy(newValues);
	}

	private static Map<String, Object> immutableCopy(Map<String, Object> source) {
		if (source == null || source.isEmpty()) {
			return Map.of();
		}
		return Collections.unmodifiableMap(new LinkedHashMap<>(source));
	}
}
