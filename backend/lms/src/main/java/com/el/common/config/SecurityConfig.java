package com.el.common.config;

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
    SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint)
            throws Exception {
        return http
                        // Define authorization rules
                        .authorizeHttpRequests(authorize -> authorize
                        // Allow all requests to /upload/** with any roles
                        .requestMatchers("/upload/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/course-paths-published/**").permitAll()
                        .requestMatchers("/course-paths/**").hasAnyRole("admin", "teacher")

                        .requestMatchers("courses/{courseId}/reviews").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/courses/{courseId}/posts",
                                "/courses/{courseId}/posts/{postId}").authenticated()
                        .requestMatchers("/courses/{courseId}/posts/{postId}/emotions/**").authenticated()
                        .requestMatchers("/courses/{courseId}/posts/{postId}/comments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/{courseId}/posts/trash").hasAnyRole("teacher", "admin")
                        .requestMatchers("/courses/{courseId}/posts/**").hasAnyRole("teacher", "admin")

                        .requestMatchers("/courses/{courseId}/requests/{requestId}/approve").hasRole("admin")
                        .requestMatchers("/courses/{courseId}/requests/{requestId}/reject").hasRole("admin")
                        .requestMatchers("/courses/{courseId}/requests",
                                "/courses/{courseId}/requests/{requestId}").hasAnyRole("admin", "teacher")

                        .requestMatchers(HttpMethod.GET, "/discounts/code/{code}").authenticated()
                        .requestMatchers("/discounts/**").hasRole("admin")

                        .requestMatchers("/users/search/**").hasAnyRole("admin", "teacher")
                        .requestMatchers("/users/count/**").hasRole("admin")

                        .requestMatchers("/actuator/health/readiness").permitAll()
                        .requestMatchers("/actuator/health/liveness").permitAll()
                        .requestMatchers("/me").permitAll()
                        // Allow GET requests to /courses/** without authentication
                        .requestMatchers(HttpMethod.GET, "/published-courses/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/enrollments/{enrollmentId}/quizzes/{quizSubmissionId}/submission").hasAnyRole("admin", "teacher")
                        .requestMatchers("/enrollments/statistics/**").hasAnyRole("admin", "teacher")
                        .requestMatchers("/enrollments/**").authenticated()

                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/payments/**").authenticated()

                        .requestMatchers(HttpMethod.PUT, "/courses/{courseId}/update-price").hasRole("admin")
                        .requestMatchers(HttpMethod.PUT, "/courses/{courseId}/assign-teacher").hasRole("admin")

                        .requestMatchers("/teachers").hasRole("admin")
                        .requestMatchers("/teachers/{teacher}").hasAnyRole("admin", "teacher")
                        .requestMatchers("/salaries/**").hasRole("admin")

                        // Require "teacher" role for all other requests
                        .anyRequest().hasAnyRole("teacher", "admin")
                )
                // Custom exception handling
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                // Configure OAuth2 resource server to use JWT tokens
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        // Set the JWT authentication converter
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
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
