package com.el.common.config;

import com.el.common.LmsProperties;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak(LmsProperties properties) {
        return KeycloakBuilder.builder()
                .serverUrl(properties.keycloak().serverUrl())
                .realm(properties.keycloak().realm())
                .clientId(properties.keycloak().clientId())
                .clientSecret(properties.keycloak().clientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

}
