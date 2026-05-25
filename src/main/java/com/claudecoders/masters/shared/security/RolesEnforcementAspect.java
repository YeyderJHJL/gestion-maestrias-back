package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.UserRole;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Enforces @Authorize and @Public annotations on all @RestController methods.
 * Disabled in dev/test profiles where the SecurityConfig permits everything.
 *
 * Default behavior (no annotation): only ADMIN may access.
 */
@Aspect
@Component
@Profile("!(dev | test)")
public class RolesEnforcementAspect {

	@Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(* *(..))")
	public Object enforce(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		Class<?> beanType = pjp.getTarget().getClass();

		if (isPublic(method, beanType)) {
			return pjp.proceed();
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || isAnonymous(auth)) {
			throw new AccessDeniedException("Authentication required");
		}

		UserRole[] required = requiredRoles(method, beanType);
		boolean hasRole = Arrays.stream(required)
				.anyMatch(role -> auth.getAuthorities().stream()
						.anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name())));

		if (!hasRole) {
			throw new AccessDeniedException("Insufficient role");
		}
		return pjp.proceed();
	}

	private boolean isPublic(Method method, Class<?> beanType) {
		return method.isAnnotationPresent(Public.class)
				|| AnnotationUtils.findAnnotation(beanType, Public.class) != null;
	}

	private boolean isAnonymous(Authentication auth) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
	}

	private UserRole[] requiredRoles(Method method, Class<?> beanType) {
		Authorize authorize = method.getAnnotation(Authorize.class);
		if (authorize == null) {
			authorize = AnnotationUtils.findAnnotation(beanType, Authorize.class);
		}
		return authorize != null ? authorize.roles() : new UserRole[]{UserRole.ADMIN};
	}
}
