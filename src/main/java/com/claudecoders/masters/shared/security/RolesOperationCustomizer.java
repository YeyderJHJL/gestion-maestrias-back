package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.UserRole;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class RolesOperationCustomizer implements OperationCustomizer {

	private static final String SCHEME = "bearerAuth";
	private static final String ROLE_PREFIX = "**Roles requeridos:** ";
	private static final String PUBLIC_NOTE = "> Acceso público — no se requiere autenticación.";
	private static final String DEFAULT_NOTE = "> Acceso restringido a **ADMIN** (por defecto).";

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		Public pub = resolve(handlerMethod, Public.class);
		if (pub != null) {
			return prepend(operation, PUBLIC_NOTE);
		}

		Authorize authorize = resolve(handlerMethod, Authorize.class);
		if (authorize != null) {
			String roles = Arrays.stream(authorize.roles())
					.map(UserRole::name)
					.collect(Collectors.joining(", "));
			String note = ROLE_PREFIX + roles;
			if (!authorize.description().isBlank()) {
				note += "\\\n> " + authorize.description();
			}
			return prepend(operation, note).addSecurityItem(new SecurityRequirement().addList(SCHEME));
		}

		return prepend(operation, DEFAULT_NOTE).addSecurityItem(new SecurityRequirement().addList(SCHEME));
	}

	private <A extends java.lang.annotation.Annotation> A resolve(HandlerMethod m, Class<A> type) {
		A annotation = m.getMethodAnnotation(type);
		if (annotation == null) {
			annotation = AnnotationUtils.findAnnotation(m.getBeanType(), type);
		}
		return annotation;
	}

	private Operation prepend(Operation op, String note) {
		String existing = op.getDescription() != null ? "\n\n" + op.getDescription() : "";
		op.setDescription(note + existing);
		return op;
	}
}
