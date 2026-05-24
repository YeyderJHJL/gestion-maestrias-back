package com.claudecoders.masters.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard envelope for successful API responses")
public record ApiResponse<T>(
		boolean success,
		T data,
		String message
) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> ok(T data, String message) {
		return new ApiResponse<>(true, data, message);
	}
}
