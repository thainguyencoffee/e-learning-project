package com.el.common.auth.application.impl;

import com.el.common.auth.application.UsersManagement;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakUsersManagement implements UsersManagement {

    @Value("${realm_name}")
    private String realmName;

    private final Keycloak keycloak;

    @Override
    public Integer count() {
        return keycloak.realm(realmName).users().count();
    }

    @Override
    public Integer count(String search) {
        return keycloak.realm(realmName).users().count(search);
    }

    @Override
    public Integer count(String lastName, String firstName, String email, String username) {
        return keycloak.realm(realmName).users().count(lastName, firstName, email, username);
    }

    @Override
    public Integer countEmailVerified(Boolean emailVerified) {
        return keycloak.realm(realmName).users().countEmailVerified(emailVerified);
    }

    @Override
    public List<UserRepresentation> search(String username, Boolean exact) {
        return keycloak.realm(realmName).users().searchByUsername(username, exact);
    }

    @Override
    public List<UserRepresentation> searchByEmail(String email, Boolean exact) {
        return keycloak.realm(realmName).users().searchByEmail(email, exact);
    }

    @Override
    public List<UserRepresentation> searchByFirstName(String firstName, Boolean exact) {
        return keycloak.realm(realmName).users().searchByFirstName(firstName, exact);
    }

    @Override
    public List<UserRepresentation> searchByLastName(String lastName, Boolean exact) {
        return keycloak.realm(realmName).users().searchByLastName(lastName, exact);
    }

    @Override
    public List<UserRepresentation> search(String search, Integer first, Integer max) {
        return keycloak.realm(realmName).users().search(search, first, max);
    }


}
