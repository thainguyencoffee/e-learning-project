package com.el;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(LmsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
