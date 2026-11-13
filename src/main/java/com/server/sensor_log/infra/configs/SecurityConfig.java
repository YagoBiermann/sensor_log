package com.server.sensor_log.infra.configs;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import io.jsonwebtoken.security.Keys;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((var auth) -> auth
                        .requestMatchers(HttpMethod.POST,"/api/devices/*/claim").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/devices/{serial}/code")
                        .access((authentication, context) -> {
                            var currentAuthentication = authentication.get();
                            String serial = context.getVariables().get("serial");
                            boolean isAdminOrUser = isAdmin(currentAuthentication) || isUser(currentAuthentication);
                            boolean isMatchingDevice = isDevice(currentAuthentication)
                                    && serialMatches(currentAuthentication, serial);
                            return new AuthorizationDecision(isAdminOrUser || isMatchingDevice);
                        })
                        .requestMatchers(HttpMethod.POST,"/api/devices/{serial}/enrollment")
                        .access((authentication, context) -> {
                            var currentAuthentication = authentication.get();
                            String serial = context.getVariables().get("serial");
                            return new AuthorizationDecision(
                                    isDevice(currentAuthentication) && serialMatches(currentAuthentication, serial));
                        })
                        .requestMatchers(HttpMethod.POST,"/api/devices/*/revoke").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/devices/*").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST,"/api/devices").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST,"/api/auth/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/devices/*/boot").hasRole("DEVICE")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    private boolean serialMatches(Authentication authentication, String serial) {
        return authentication.getPrincipal() instanceof Jwt jwt
                && serial != null
                && serial.equals(jwt.getSubject());
    }

    private boolean isDevice(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DEVICE"));
    }

    private boolean isUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}