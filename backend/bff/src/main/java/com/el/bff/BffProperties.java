package com.el.bff;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bff")
public record BffProperties(
    String greeting
) {
}
