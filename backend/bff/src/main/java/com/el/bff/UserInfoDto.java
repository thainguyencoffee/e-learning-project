package com.el.bff;

import java.util.List;

public record UserInfoDto(
        String username,
        String firstName,
        String lastName,
        String email,
        List<String> roles,
        Long exp
) {

    public static UserInfoDto anonymous() {
        return new UserInfoDto(
                "",
                "",
                "",
                "",
                List.of(),
                null
        );
    }

}
