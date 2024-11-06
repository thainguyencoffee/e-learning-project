package com.el;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class KeycloakTestContainer {

    private static KeycloakContainer keycloak;

    public static KeycloakContainer getInstance() {
        if (keycloak == null) {
            keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("keycloak101-realm.json")
                    .withEnv("KEYCLOAK_ADMIN", "admin")
                    .withEnv("KEYCLOAK_ADMIN_PASSWORD", "secret");
            keycloak.start();
        }
        return keycloak;
    }

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> getInstance().getAuthServerUrl() + "/realms/keycloak101");
        registry.add("reverse-proxy-uri", getInstance()::getAuthServerUrl);
        registry.add("authorization-server-prefix", () -> "");
    }

}
