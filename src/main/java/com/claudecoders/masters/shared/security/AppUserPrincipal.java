package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.shared.enums.UserRole;
import java.util.UUID;

public record AppUserPrincipal(UUID userId, UserRole role) {}
