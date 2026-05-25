package com.claudecoders.masters.shared.security;

import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public AppJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String googleSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        User user = userRepository.findByGoogleSub(googleSub)
                .or(() -> userRepository.findByEmail(email)
                        .map(u -> linkGoogleSub(u, jwt)))
                .orElseThrow(() -> new AccessDeniedException("Usuario no registrado en el sistema"));

        if (!user.getActive()) {
            throw new AccessDeniedException("Usuario inactivo");
        }

        var principal = new AppUserPrincipal(user.getId(), user.getRole());
        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new UsernamePasswordAuthenticationToken(principal, jwt, List.of(authority));
    }

    // Vincula el google_sub real al primer login del usuario.
    // Actualiza nombres solo cuando el google_sub era un placeholder del seeder.
    private User linkGoogleSub(User user, Jwt jwt) {
        boolean wasPlaceholder = user.getGoogleSub() == null
                || user.getGoogleSub().startsWith("pending-");
        user.setGoogleSub(jwt.getSubject());
        if (wasPlaceholder) {
            String given = jwt.getClaimAsString("given_name");
            String family = jwt.getClaimAsString("family_name");
            if (given != null && !given.isBlank()) user.setFirstName(given);
            if (family != null && !family.isBlank()) user.setLastName(family);
        }
        return userRepository.save(user);
    }
}
