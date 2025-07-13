package ru.mityunin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.dto.UserDto;
import ru.mityunin.service.AccountsService;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final AccountsService accountsService;

    public HomeController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }
    @GetMapping
    public String showMainPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserDto userDto = accountsService.getUserByLogin(username);
            model.addAttribute("userDto", userDto);
        }
        return "main";
    }
}
