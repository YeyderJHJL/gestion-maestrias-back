package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.UserRole;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which roles may access an endpoint.
 * When omitted, only {@link UserRole#ADMIN} is allowed (default).
 * Combine with {@link Public} to open an endpoint without authentication.
 *
 * <p>Example:
 * <pre>{@code
 * @Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER}, description = "Crear un nuevo curso")
 * @PostMapping
 * public ApiResponse<CourseResponse> create(...) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorize {

	UserRole[] roles() default {UserRole.ADMIN};

	String description() default "";
}
