package com.el.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lms")
public record LmsProperties(
        String greeting,
        Keycloak keycloak
) {

    public record Keycloak(
            String serverUrl,
            String realm,
            String clientId,
            String clientSecret
    ){}
}
