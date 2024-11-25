package com.el;

import com.el.common.LmsProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HomeController {

    private final LmsProperties lmsProperties;

    public HomeController(LmsProperties lmsProperties) {
        this.lmsProperties = lmsProperties;
    }

    @GetMapping("/greeting")
    public String greeting() {
        return lmsProperties.greeting();
    }

}
