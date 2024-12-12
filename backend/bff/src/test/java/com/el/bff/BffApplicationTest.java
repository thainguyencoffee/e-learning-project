package com.el.bff;

import com.c4_soft.springaddons.security.oauth2.test.webflux.AddonsWebfluxTestConf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@ImportAutoConfiguration(AddonsWebfluxTestConf.class)
class BffApplicationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithAnonymousUser
    void givenRequestIsAnonymous_whenGetLoginOptions_thenOk() throws Exception {
        webTestClient.get().uri("/login-options")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$")
                .isArray()
                .jsonPath("$[0].label")
                .isEqualTo("keycloak")
                .jsonPath("$[0].loginUri")
                .isEqualTo("http://localhost:7081/oauth2/authorization/bff-service")
                .jsonPath("$[0].isSameAuthority")
                .isEqualTo(false);
    }

    @Test
    @WithAnonymousUser
    void givenRequestIsAnonymous_whenGetLiveness_thenOk() throws Exception {
        webTestClient.get().uri("/actuator/health/liveness")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithAnonymousUser
    void givenRequestIsAnonymous_whenGetReadiness_thenOk() throws Exception {
        webTestClient.get().uri("/actuator/health/readiness")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
