package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.UserRole;
import java.util.UUID;

public record AppUserPrincipal(UUID userId, UserRole role) {}
