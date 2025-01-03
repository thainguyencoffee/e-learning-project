package com.el.common.auth.application;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsersManagement {

    Integer count();

    Integer count(String search);

    Integer count(String lastName, String firstName, String email, String username);

    Integer countEmailVerified(Boolean emailVerified);

    // *********
    List<UserRepresentation> search(String username, Boolean exact);

    List<UserRepresentation> search(String username, Boolean exact, String roleName, Pageable pageable);

    List<UserRepresentation> searchByRole(String roleName, Pageable pageable);

    UserRepresentation getUser(String username);
}
