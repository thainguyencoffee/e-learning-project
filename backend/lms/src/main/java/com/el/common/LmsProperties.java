package com.el.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lms")
public record LmsProperties(
        String greeting
) {
}
