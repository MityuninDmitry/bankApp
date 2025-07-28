package ru.mityunin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrency;
import ru.mityunin.dto.UserDto;
import ru.mityunin.service.AccountsService;
import ru.mityunin.service.ExchangeGeneratorService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final AccountsService accountsService;
    private final ExchangeGeneratorService exchangeGeneratorService;

    public HomeController(AccountsService accountsService, ExchangeGeneratorService exchangeGeneratorService) {
        this.accountsService = accountsService;
        this.exchangeGeneratorService = exchangeGeneratorService;
    }
    @GetMapping
    public String showMainPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserDto userDto = accountsService.getUserByLogin(username);
            ApiResponse<List<ExchangeCurrency>> response = exchangeGeneratorService.currencies();

            if (response != null && response.isSuccess() && response.getData() != null) {
                model.addAttribute("currencies", response.getData());
            } else {
                model.addAttribute("currencies", Collections.emptyList());
            }
            model.addAttribute("userDto", userDto);
        }
        return "main";
    }
}
