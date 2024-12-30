package com.el.bff;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class LoginOptionsController {

    @Value("${client-uri}")
    private String clientUri;

    @GetMapping(path = "/login-options", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<LoginOptionDto>> getLoginOptions() {
        return Mono.just(List.of(new LoginOptionDto("e-learning-project",
                "%s/oauth2/authorization/%s".formatted(this.clientUri, "keycloak101"), false)));
    }

    public record LoginOptionDto(@NotEmpty String label, @NotEmpty String loginUri, boolean isSameAuthority) {
    }

}