package com.el.common.auth.web;

import com.el.common.auth.application.impl.KeycloakUsersManagement;
import com.el.common.auth.web.dto.UserInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResourceController {

    private final KeycloakUsersManagement keycloakUsersManagement;

    public UserResourceController(KeycloakUsersManagement keycloakUsersManagement) {
        this.keycloakUsersManagement = keycloakUsersManagement;
    }

    @GetMapping("/count")
    public Integer countUsersWithParams(
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "username", required = false) String username) {
        if (lastName == null && firstName == null && email == null && username == null) {
            return keycloakUsersManagement.count();
        }

        return keycloakUsersManagement.count(lastName, firstName, email, username);
    }

    @GetMapping("/count/search")
    public Integer countUsersWithSearch(
            @RequestParam(value = "search", required = false) String search) {
        if (search == null) {
            return keycloakUsersManagement.count();
        }

        return keycloakUsersManagement.count(search);
    }

    @GetMapping("/search")
    public List<UserInfo> searchUsersWithSearch(
            @RequestParam("username") String username,
            @RequestParam(value = "exact", required = false, defaultValue = "false") Boolean exact,
            @RequestParam(value = "role", required = false) String roleName, Pageable pageable) {

        if (roleName != null && !roleName.isBlank()) {
            return keycloakUsersManagement.search(username, exact, roleName, pageable)
                    .stream().map(UserInfo::fromUserRepresentation)
                    .collect(Collectors.toList());

        }
        return keycloakUsersManagement.search(username, exact)
                .stream().map(UserInfo::fromUserRepresentation)
                .collect(Collectors.toList());
    }


}
