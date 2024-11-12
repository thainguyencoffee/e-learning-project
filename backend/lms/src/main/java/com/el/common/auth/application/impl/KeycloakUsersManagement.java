package com.el.common.auth.application.impl;

import com.el.common.auth.application.UsersManagement;
import com.el.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<UserRepresentation> search(String username, Boolean exact, String roleName) {
        List<UserRepresentation> users = keycloak.realm(realmName).users().searchByUsername(username, exact);
        return users.stream()
                .filter(user -> hasRole(user, roleName))
                .collect(Collectors.toList());
    }

    @Override
    public UserRepresentation getUser(String username) {
        List<UserRepresentation> users = keycloak.realm(realmName).users().searchByUsername(username, true);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        return users.get(0);
    }

    private boolean hasRole(UserRepresentation user, String roleName) {
        List<RoleRepresentation> roles = keycloak.realm(realmName)
                .users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .listAll();
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
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
