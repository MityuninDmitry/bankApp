package ru.mityunin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.*;
import ru.mityunin.service.AccountsService;

import java.util.List;

@Controller
@RequestMapping("/frontui/home")
public class HomeController {
    private final AccountsService accountsService;

    public HomeController(
            AccountsService accountsService) {
        this.accountsService = accountsService;
    }
    @GetMapping
    public String showMainPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserDto userDto = accountsService.getUserByLogin(username);
            ApiResponse<List<UserDto>> allUsers = accountsService.getAllUsersExcept(username);

            model.addAttribute("userDto", userDto);
            model.addAttribute("allUsers", allUsers.getData());
        }
        return "main";
    }
}
