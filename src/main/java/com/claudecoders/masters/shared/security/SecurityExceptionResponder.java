package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.shared.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Writes ApiError JSON for security-layer failures (filter phase, before MVC).
 * Implements both AuthenticationEntryPoint (401) and AccessDeniedHandler (403)
 * so all security errors share the same ApiError response format.
 */
@Component
public class SecurityExceptionResponder implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException ex) throws IOException {
		write(response, HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException ex) throws IOException {
		write(response, HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}

	private void write(HttpServletResponse response, HttpStatus status,
			String message, String path) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiError.of(status, message, path));
	}
}
