package com.elearning;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ResourceServerApplicationTests {

    protected static KeycloakToken userToken;
    protected static KeycloakToken user2Token;
    protected static KeycloakToken teacherToken;

    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("thainguyencoffee-realm.json");

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/thainguyencoffee");
    }

    @BeforeAll
    static void generateAccessToken() {
        keycloak.start(); // Fix: Mapped port can only be obtained after the container is started

        WebClient webClient = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl() + "/realms/thainguyencoffee/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        userToken   = authenticateWith("user", "1", webClient);
        user2Token = authenticateWith("user2", "1", webClient);
        teacherToken = authenticateWith("teacher", "1", webClient);
    }

    @Test
    void contextLoads() {
        assertThat(userToken.accessToken).isNotNull();
        assertThat(user2Token.accessToken).isNotNull();
        assertThat(teacherToken.accessToken).isNotNull();
    }

    protected static class KeycloakToken {
        private final String accessToken;

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    private static KeycloakToken authenticateWith(
            String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "thainguyencoffee-confidentials")
                        .with("client_secret", "qk2lxjuIPAUY0e9I1AMzQZLQf3YINJ80")
                        .with("username", username)
                        .with("password", password)
                )
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

}
