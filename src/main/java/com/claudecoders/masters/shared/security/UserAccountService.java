package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a Google JWT subject to an AppUserPrincipal.
 * Results are cached by googleSub (5-min TTL) to avoid a DB round-trip on every request.
 * First-login: links the real googleSub to an existing user found by email (pending-* placeholder).
 */
@Service
public class UserAccountService {

	private final UserRepository userRepository;

	public UserAccountService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Cacheable(value = "userPrincipals", key = "#googleSub")
	@Transactional
	public CachedPrincipal resolveUser(String googleSub, String email,
			String givenName, String familyName) {
		User user = userRepository.findByGoogleSub(googleSub)
				.or(() -> userRepository.findByEmail(email)
						.map(u -> linkGoogleSub(u, googleSub, givenName, familyName)))
				.orElseThrow(() -> new InvalidBearerTokenException("User not registered"));

		if (!user.getActive()) {
			throw new InvalidBearerTokenException("Inactive user");
		}
		return new CachedPrincipal(user.getId(), user.getRole());
	}

	@CacheEvict(value = "userPrincipals", key = "#googleSub")
	public void evictUser(String googleSub) {}

	private User linkGoogleSub(User user, String googleSub, String givenName, String familyName) {
		boolean wasPlaceholder = user.getGoogleSub() == null
				|| user.getGoogleSub().startsWith("pending-");
		user.setGoogleSub(googleSub);
		if (wasPlaceholder) {
			if (givenName != null && !givenName.isBlank()) user.setFirstName(givenName);
			if (familyName != null && !familyName.isBlank()) user.setLastName(familyName);
		}
		return userRepository.save(user);
	}
}
