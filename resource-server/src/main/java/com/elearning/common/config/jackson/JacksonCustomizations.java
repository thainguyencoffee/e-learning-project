package com.elearning.common.config.jackson;

import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonCustomizations {

    @Bean
    Module moneyModule() {
        return new MoneyModule();
    }

    @Bean
    Module dateTimeModule() {
        return new DateTimeModule();
    }

}
