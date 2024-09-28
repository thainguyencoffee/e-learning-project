package com.elearning.userauthorization;

import java.time.Instant;
import java.util.List;

public record UserInfoDto(
        String fullName,
        String username,
        String email,
        List<String> roles,
        Instant exp
) {
    public static final UserInfoDto ANONYMOUS =
            new UserInfoDto("", "", "", List.of(), Instant.EPOCH);

}