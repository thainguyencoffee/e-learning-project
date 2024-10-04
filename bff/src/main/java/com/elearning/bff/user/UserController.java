package com.elearning.bff.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    @GetMapping("user")
    public Mono<User> getUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return Mono.just(new User(
                    null,
                    null,
                    null,
                    null
            ));
        } else {
            return Mono.just(new User(
                    oidcUser.getPreferredUsername(),
                    oidcUser.getGivenName(),
                    oidcUser.getFamilyName(),
                    oidcUser.getClaimAsStringList("roles")
            ));
        }
    }

}
