package com.claudecoders.masters.shared.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

	public Optional<UUID> currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UUID userId) {
			return Optional.of(userId);
		}
		if (principal instanceof Jwt jwt) {
			return firstUuid(
					jwt.getClaimAsString("id_user"),
					jwt.getClaimAsString("user_id"),
					jwt.getClaimAsString("userId"),
					jwt.getClaimAsString("uid"),
					jwt.getSubject()
			);
		}
		if (principal instanceof OidcUser oidcUser) {
			return firstUuid(
					oidcUser.getAttribute("id_user"),
					oidcUser.getAttribute("user_id"),
					oidcUser.getAttribute("userId"),
					oidcUser.getAttribute("uid"),
					oidcUser.getSubject()
			);
		}
		if (principal instanceof OAuth2AuthenticatedPrincipal oauthPrincipal) {
			return firstUuid(
					oauthPrincipal.getAttribute("id_user"),
					oauthPrincipal.getAttribute("user_id"),
					oauthPrincipal.getAttribute("userId"),
					oauthPrincipal.getAttribute("uid"),
					oauthPrincipal.getAttribute("sub")
			);
		}
		return parseUuid(authentication.getName());
	}

	private Optional<UUID> firstUuid(Object... values) {
		for (Object value : values) {
			Optional<UUID> userId = parseUuid(value);
			if (userId.isPresent()) {
				return userId;
			}
		}
		return Optional.empty();
	}

	private Optional<UUID> parseUuid(Object value) {
		if (value instanceof UUID uuid) {
			return Optional.of(uuid);
		}
		if (!(value instanceof String text) || text.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(text));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}
}
