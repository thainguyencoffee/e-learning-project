package com.elearning.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Define authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Allow GET requests to /profiles/** without authentication
                        .requestMatchers(HttpMethod.GET, "/profile/**").permitAll()

                        // Allow GET requests to /courses/** without authentication
                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/courses/{courseId}/update-price").hasRole("admin")
                        .requestMatchers(HttpMethod.PUT, "/courses/{courseId}/assign-teacher").hasRole("admin")

                        // Require "teacher" role for all other requests
                        .anyRequest().hasAnyRole("teacher", "admin")
                )
                // Configure OAuth2 resource server to use JWT tokens
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        // Set the JWT authentication converter
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Set session management policy to stateless
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Disable CSRF protection
                .csrf(AbstractHttpConfigurer::disable)
                // Build and return the SecurityFilterChain
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Create a converter for extracting authorities from the JWT token
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Prefix extracted authorities with "ROLE_"
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // Set the claim name that contains the authorities
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        // Create the JWT authentication converter and set the authorities converter
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        // Return the configured JWT authentication converter
        return jwtAuthenticationConverter;
    }

}
