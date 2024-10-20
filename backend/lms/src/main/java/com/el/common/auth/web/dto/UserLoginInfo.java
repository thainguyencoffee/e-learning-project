package com.el.common.auth.web.dto;


import java.util.List;

public record UserLoginInfo(String username, String email, List<String> roles, Long exp) {

    public static UserLoginInfo anonymous() {
        return new UserLoginInfo("","", List.of(),null);
    }



}
