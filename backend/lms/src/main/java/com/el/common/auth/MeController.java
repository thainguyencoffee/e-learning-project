package com.el.common.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@RestController
public class MeController {

    @GetMapping("/me")
    public UserInfoDto getMe(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            final var email = (String) jwtAuth.getTokenAttributes()
                    .getOrDefault(StandardClaimNames.EMAIL, "");
            final var roles = auth.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            final var exp = Optional.ofNullable(jwtAuth.getTokenAttributes()
                    .get(JwtClaimNames.EXP)).map(expClaim -> {
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
            }).orElse(Long.MAX_VALUE);
            return new UserInfoDto(auth.getName(), email, roles, exp);
        }
        return UserInfoDto.anonymous();
    }

}
