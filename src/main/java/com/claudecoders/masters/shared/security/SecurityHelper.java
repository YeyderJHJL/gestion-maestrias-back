package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.shared.exception.UnauthorizedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityHelper {

    private SecurityHelper() {}

    public static UUID currentUserId() {
        return currentPrincipal().userId();
    }

    public static AppUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal;
        }
        throw new UnauthorizedException("Authentication required");
    }
}
