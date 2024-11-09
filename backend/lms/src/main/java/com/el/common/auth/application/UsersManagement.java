package com.el.common.auth.application;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UsersManagement {

    Integer count();

    Integer count(String search);

    Integer count(String lastName, String firstName, String email, String username);

    Integer countEmailVerified(Boolean emailVerified);

    List<UserRepresentation> search(String username, Boolean exact);

    List<UserRepresentation> search(String username, Boolean exact, String roleName);

    List<UserRepresentation> searchByEmail(String email, Boolean exact);

    List<UserRepresentation> searchByFirstName(String firstName, Boolean exact);

    List<UserRepresentation> searchByLastName(String lastName, Boolean exact);

    List<UserRepresentation> search(String search, Integer first, Integer max);

}
