package com.claudecoders.masters.shared.config;

import com.claudecoders.masters.shared.security.AppJwtAuthenticationConverter;
import com.claudecoders.masters.shared.security.SecurityExceptionResponder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Dev/test: permite toda solicitud sin autenticación obligatoria.
 * Si se envía un JWT válido de Google, sí se valida y se establece el principal
 * (AppUserPrincipal), lo que permite probar el flujo completo con el frontend de prueba.
 * Requests sin JWT continúan como anónimos.
 */
@Configuration(proxyBeanMethods = false)
@Profile({ "dev", "test" })
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            AppJwtAuthenticationConverter converter,
            SecurityExceptionResponder responder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(responder))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(responder)
                        .accessDeniedHandler(responder));
        return http.build();
    }
}
