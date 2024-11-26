package com.el.bff;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HomeController {

    private final BffProperties bffProperties;

    public HomeController(BffProperties bffProperties) {
        this.bffProperties = bffProperties;
    }

    @GetMapping("/greeting")
    public String greeting() {
        return bffProperties.greeting();
    }

}
