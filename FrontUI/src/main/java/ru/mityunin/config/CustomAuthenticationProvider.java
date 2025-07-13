package ru.mityunin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.mityunin.dto.UserDto;
import ru.mityunin.service.AccountsService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    private final AccountsService accountsService;
    private final UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(AccountsService accountsService,
                                        UserDetailsService userDetailsService) {
        this.accountsService = accountsService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (authentication.getCredentials() != null)
                ? authentication.getCredentials().toString()
                : null;

        // Валидация введенных данных
        if (password == null || password.isEmpty()) {
            throw new BadCredentialsException("Password cannot be empty");
        }

        // Аутентификация (проверка логина/пароля)
        UserDto userDto = accountsService.authenticate(username, password);
        if (userDto == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Загрузка UserDetails (после успешной аутентификации)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // Пароль очищаем после проверки
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}