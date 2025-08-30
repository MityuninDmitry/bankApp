package ru.mityunin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationProvider authProvider;

    public SecurityConfig(CustomAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/frontui/",                       // ← Добавлено /frontui
                                "/frontui/login",                  // ← Добавлено /frontui
                                "/frontui/accounts/register",      // ← Добавлено /frontui
                                "/frontui/static/**",              // ← Добавлено /frontui
                                "/frontui/js/**",                  // ← Добавлено /frontui
                                "/frontui/css/**",                 // ← Добавлено /frontui
                                "/frontui/webjars/**"              // ← Добавлено /frontui
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/frontui/accounts/register").permitAll()  // ← Добавлено /frontui
                        .requestMatchers(HttpMethod.POST, "/frontui/delete").authenticated()         // ← Добавлено /frontui
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/frontui/login")               // ← Добавлено /frontui
                        .loginProcessingUrl("/frontui/perform_login")  // ← Добавлено /frontui
                        .defaultSuccessUrl("/frontui/home", true)  // ← Добавлено /frontui
                        .failureUrl("/frontui/login?error=true")   // ← Добавлено /frontui
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/frontui/logout")              // ← Добавлено /frontui
                        .logoutSuccessUrl("/frontui/login?logout") // ← Добавлено /frontui
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}