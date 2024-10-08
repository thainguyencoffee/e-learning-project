package com.el.bff;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@RestController
public class MeController {

    @GetMapping("me")
    public Mono<UserInfoDto> me(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return Mono.just(UserInfoDto.anonymous());
        } else {
            final var exp = Optional.ofNullable(oidcUser.getClaims().get("exp"))
                    .map(expClaim -> {
                        if(expClaim instanceof Long lexp) {
                            return lexp;
                        }
                        if(expClaim instanceof Instant iexp) {
                            return iexp.getEpochSecond();
                        }
                        if(expClaim instanceof Date dexp) {
                            return dexp.toInstant().getEpochSecond();
                        }
                        return Long.MAX_VALUE;
                    })
                    .orElse(Long.MAX_VALUE);

            return Mono.just(new UserInfoDto(
                    oidcUser.getPreferredUsername(),
                    oidcUser.getGivenName(),
                    oidcUser.getFamilyName(),
                    oidcUser.getEmail(),
                    oidcUser.getClaimAsStringList("roles"),
                    exp
            ));
        }
    }

}
