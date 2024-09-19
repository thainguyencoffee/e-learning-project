package com.elearning;

import org.springframework.boot.SpringApplication;

public class TestELearningApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
