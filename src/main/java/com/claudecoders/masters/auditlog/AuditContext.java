package com.claudecoders.masters.auditlog;

public final class AuditContext {

	private static final ThreadLocal<String> reasonHolder = new ThreadLocal<>();

	private AuditContext() {
	}

	public static void setReason(String reason) {
		reasonHolder.set(reason);
	}

	public static String getReason() {
		return reasonHolder.get();
	}

	public static void clear() {
		reasonHolder.remove();
	}
}