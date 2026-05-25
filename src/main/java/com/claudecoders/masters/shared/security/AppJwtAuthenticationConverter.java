package com.claudecoders.masters.shared.security;

import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AppJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UserAccountService userAccountService;

	public AppJwtAuthenticationConverter(UserAccountService userAccountService) {
		this.userAccountService = userAccountService;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		CachedPrincipal cp = userAccountService.resolveUser(
				jwt.getSubject(),
				jwt.getClaimAsString("email"),
				jwt.getClaimAsString("given_name"),
				jwt.getClaimAsString("family_name")
		);
		var principal = new AppUserPrincipal(cp.id(), cp.role());
		var authority = new SimpleGrantedAuthority("ROLE_" + cp.role().name());
		return new UsernamePasswordAuthenticationToken(principal, jwt, List.of(authority));
	}
}
