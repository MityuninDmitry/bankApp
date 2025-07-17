package ru.mityunin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.service.AccountsService;

import java.time.LocalDate;
import java.time.Period;

@Controller
@RequestMapping("/accounts")
public class AccountsController {
    private final AccountsService accountsService;
    private final UserDetailsService userDetailsService;

    public AccountsController(AccountsService accountsService, UserDetailsService userDetailsService) {
        this.accountsService = accountsService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/delete")
    public String deleteUser(Authentication authentication,
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

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new UserRegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationRequest") UserRegistrationRequest registrationRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {

        // Дополнительная проверка возраста (18+)
        if (registrationRequest.getBirthDate() != null &&
                Period.between(registrationRequest.getBirthDate(), LocalDate.now()).getYears() < 18) {
            bindingResult.rejectValue("birthDate", "error.registrationRequest", "You must be at least 18 years old");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        UserDto registeredUser = accountsService.registerUser(registrationRequest);
        if (registeredUser == null) {
            model.addAttribute("error", "Registration failed. Login might be already taken.");
            return "register";
        }

        // Автоматическая аутентификация после регистрации
        UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getLogin());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                registrationRequest.getPassword(),
                userDetails.getAuthorities());

        // Устанавливаем аутентификацию в SecurityContext
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        // Создаем новую сессию и сохраняем SecurityContext
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        return "redirect:/home";
    }

    @PostMapping("/update/password")
    public String updatePassword(AuthRequest authRequest, Model model) {
        boolean success = accountsService.updatePassword(authRequest);
        if (success) {
            model.addAttribute("passwordUpdated", true);
        } else {
            model.addAttribute("passwordError", "Failed to update password");
        }
        return "redirect:/home";
    }

    @PostMapping("/update/userInfo")
    public String updateUserInfo(UserDto userDto, Model model) {
        boolean success = accountsService.updateUserInfo(userDto);
        if (success) {
            model.addAttribute("userInfoUpdated", true);
        } else {
            model.addAttribute("userInfoUpdatedError", "Failed to update user info");
        }
        return "redirect:/home";
    }

    @PostMapping("/update/userInfo/deleteAccount")
    public String deleteAccount(@RequestParam String accountNumber, RedirectAttributes redirectAttributes) {

        boolean isDeleted = accountsService.deletePaymentAccount(accountNumber);

        if (!isDeleted) {
            redirectAttributes.addFlashAttribute("accountDeletionError",
                    "Account balance should be zero or service not available, try later");
        }

        return "redirect:/home";
    }

    @PostMapping("/update/userInfo/addAccount")
    public String addAccount(@RequestParam String accountNumber, Model model) {

        accountsService.addPaymentAccount(accountNumber);

        return "redirect:/home";
    }
}
