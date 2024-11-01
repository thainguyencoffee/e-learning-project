package com.el.common.auth.web.dto;

import org.keycloak.representations.idm.UserRepresentation;

public record UserInfo (
        String id,
        String firstName,
        String lastName,
        String username,
        String email
) {

    public static UserInfo fromUserRepresentation(UserRepresentation userRepresentation) {
        return new UserInfo(
                userRepresentation.getId(),
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getUsername(),
                userRepresentation.getEmail()
        );
    }

}
