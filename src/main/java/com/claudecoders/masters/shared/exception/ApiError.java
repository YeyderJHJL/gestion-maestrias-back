package com.claudecoders.masters.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

@Schema(description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
		int status,
		String error,
		String message,
		String path,
		Instant timestamp,
		Map<String, String> validationErrors
) {

	public static ApiError of(HttpStatus status, String message, String path) {
		return new ApiError(status.value(), status.getReasonPhrase(), message, path, Instant.now(), null);
	}

	public static ApiError of(
			HttpStatus status,
			String message,
			String path,
			Map<String, String> validationErrors
	) {
		return new ApiError(status.value(), status.getReasonPhrase(), message, path, Instant.now(), validationErrors);
	}
}
