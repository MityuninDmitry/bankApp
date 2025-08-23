package ru.mityunin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.*;
import ru.mityunin.service.AccountsService;
import ru.mityunin.service.ExchangeService;
import ru.mityunin.service.NotificationsService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final AccountsService accountsService;
    private final ExchangeService exchangeService;

    public HomeController(AccountsService accountsService, ExchangeService exchangeService, NotificationsService notificationsService) {
        this.accountsService = accountsService;
        this.exchangeService = exchangeService;
    }
    @GetMapping
    public String showMainPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserDto userDto = accountsService.getUserByLogin(username);
            ApiResponse<List<UserDto>> allUsers = accountsService.getAllUsersExcept(username);

            ApiResponse<List<ExchangeCurrencyFrontUIDto>> response = exchangeService.currencies();
            if (response != null && response.isSuccess() && response.getData() != null) {
                model.addAttribute("currencies", response.getData());
            } else {
                model.addAttribute("currencies", Collections.emptyList());
            }

            model.addAttribute("userDto", userDto);
            model.addAttribute("allUsers", allUsers.getData());
        }
        return "main";
    }
}
