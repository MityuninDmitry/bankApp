package ru.mityunin.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mityunin.dto.UserDto;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountsService accountsService;

    public CustomUserDetailsService(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = accountsService.getUserByLogin(username);

        if (userDto == null) {
            throw new UsernameNotFoundException("User not found with login: " + username);
        }

        return User.withUsername(userDto.getLogin())
                .password("") // Пароль не важен, так как проверяется в AuthenticationProvider
                .roles("USER")
                .build();
    }
}