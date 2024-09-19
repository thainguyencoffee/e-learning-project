package com.elearning;

import org.springframework.boot.SpringApplication;

public class TestResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.from(ResourceServerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
