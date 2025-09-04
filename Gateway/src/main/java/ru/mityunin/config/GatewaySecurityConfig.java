package ru.mityunin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(GatewaySecurityConfig.class);
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/frontui/**").hasAuthority("SCOPE_frontui")
                        .requestMatchers("/notifications/**").hasAnyAuthority(
                                "SCOPE_accounts","SCOPE_frontui","SCOPE_blocker","SCOPE_transfer")
                        .requestMatchers("/accounts/api/**").hasAnyAuthority(
                                "SCOPE_frontui", "SCOPE_cash", "SCOPE_transfer")
                        .requestMatchers("/cash/api/**").hasAuthority("SCOPE_frontui")
                        .requestMatchers("/transfer/api/**").hasAnyAuthority(
                                "SCOPE_frontui")
                        .requestMatchers("/blocker/api/**").hasAnyAuthority(
                                "SCOPE_cash","SCOPE_transfer")
                        .requestMatchers("/exchange/api/**").hasAnyAuthority(
                                "SCOPE_frontui", "SCOPE_transfer")
                        .requestMatchers("/exchangegenerator/api/**").hasAuthority("SCOPE_exchange")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
                //.addFilterBefore(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("SCOPE_");
        authoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        // Логирование JWT
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            logger.info("Processing JWT with claims: {}", jwt.getClaims());
            logger.info("JWT scopes: {}", jwt.getClaimAsStringList("scope"));
            return authoritiesConverter.convert(jwt);
        });


        return converter;
    }

}
