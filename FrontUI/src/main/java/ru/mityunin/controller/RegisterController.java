package ru.mityunin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.service.AccountsService;

import java.time.LocalDate;
import java.time.Period;

@Controller
public class RegisterController {
    private final AccountsService accountsService;
    private final UserDetailsService userDetailsService;

    public RegisterController(AccountsService accountsService,
                              UserDetailsService userDetailsService) {
        this.accountsService = accountsService;
        this.userDetailsService = userDetailsService;
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
}