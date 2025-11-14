package ru.mityunin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/frontui/login")
    public String loginPage() {
        return "login";
    }
}