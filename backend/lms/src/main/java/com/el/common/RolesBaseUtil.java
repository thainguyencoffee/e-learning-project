package com.el.common;

import com.el.common.auth.web.dto.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class RolesBaseUtil {

    public String getCurrentPreferredUsernameFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
    }

    public UserInfo getCurrentUserInfoFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return new UserInfo(
                jwt.getSubject(),
                jwt.getClaim(StandardClaimNames.GIVEN_NAME),
                jwt.getClaim(StandardClaimNames.FAMILY_NAME),
                jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME),
                jwt.getClaimAsString("roles"));
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("roles").contains("admin");
    }

    public boolean isTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("roles").contains("teacher");
    }

    public boolean isUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("roles").contains("user");
    }

}
