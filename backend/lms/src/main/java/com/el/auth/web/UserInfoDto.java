package com.el.auth.web;

import java.util.List;

public record UserInfoDto(String username, String email, List<String> roles, Long exp) {

    public static UserInfoDto anonymous() {
        return new UserInfoDto("","", List.of(),null);
    }

}
