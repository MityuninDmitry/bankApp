package ru.mityunin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll() // Разрешаем доступ к oauth2 endpoints
                        .requestMatchers("/.well-known/**").permitAll() // Разрешаем доступ к jwks
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()); // Отключаем CSRF для API

        return http.build();
    }
}