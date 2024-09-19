package com.elearning.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/profile")
public class UserController {

    @GetMapping
    public UserInfoDto getMe(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            final var username = jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
            final var email = Optional.ofNullable(jwt.getClaimAsString(StandardClaimNames.EMAIL)).orElse("");
            final var roles = jwt.getClaimAsStringList("roles");
            final var exp = jwt.getClaimAsInstant(JwtClaimNames.EXP);
            final var fullName = jwt.getClaim(StandardClaimNames.GIVEN_NAME) + " " + jwt.getClaim(StandardClaimNames.FAMILY_NAME);

            return new UserInfoDto(fullName, username, email, roles, exp);
        }
        return UserInfoDto.ANONYMOUS;
    }

}