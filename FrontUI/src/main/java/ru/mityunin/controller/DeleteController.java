package ru.mityunin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.service.AccountsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/delete")
public class DeleteController {
    private final AccountsService accountsService;

    public DeleteController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping
    public String deleteAccount(Authentication authentication,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        if (authentication != null && authentication.isAuthenticated()) {
            String login = authentication.getName();

            // Удаляем аккаунт
            accountsService.deleteUser(login);

            // Разлогиниваем пользователя
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            // Очищаем контекст безопасности
            SecurityContextHolder.clearContext();
        }

        return "redirect:/login?delete";
    }
}