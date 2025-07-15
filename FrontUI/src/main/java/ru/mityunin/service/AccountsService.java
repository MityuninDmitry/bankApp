package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;

@Service
public class AccountsService {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);
    private final RestTemplate restTemplate;
    private final String accountsServiceUrl;

    public AccountsService(RestTemplate restTemplate,
                           @Value("${accounts.service.url}") String accountsServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountsServiceUrl = accountsServiceUrl;
    }

    // Метод для проверки логина/пароля
    public UserDto authenticate(String login, String password) {
        log.info("Authenticating user: {}", login);
        String url = accountsServiceUrl + "/accounts/auth";

        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(login);
        authRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(authRequest, headers),
                    UserDto.class
            );

            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", login, e);
            return null;
        }
    }

    // Метод для получения данных пользователя без проверки пароля
    public UserDto getUserByLogin(String login) {
        log.info("Getting user by login: {}", login);
        String url = accountsServiceUrl + "/accounts/" + login;

        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                    url,
                    UserDto.class
            );

            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            log.error("Failed to get user by login: {}", login, e);
            return null;
        }
    }

    public UserDto registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering new user: {}", registrationRequest.getLogin());
        String url = accountsServiceUrl + "/accounts/register";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(registrationRequest, headers),
                    UserDto.class
            );

            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registrationRequest.getLogin(), e);
            return null;
        }
    }

    public boolean deleteUser(String login) {
        log.info("Deleting user: {}", login);
        String url = accountsServiceUrl + "/accounts/delete";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(login, headers),
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to delete user: {}", login, e);
            return false;
        }
    }

    public boolean updatePassword(AuthRequest authRequest) {
        log.info("Updating password for user: {}", authRequest.getLogin());
        String url = accountsServiceUrl + "/accounts/update/password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(authRequest, headers),
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to update password for user: {}", authRequest.getLogin(), e);
            return false;
        }
    }

    public boolean updateUserInfo(UserDto userDto) {
        log.info("Updating user info for user: {}", userDto.getLogin());
        String url = accountsServiceUrl + "/accounts/update/userInfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(userDto, headers),
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to update user info for user: {}", userDto.getLogin(), e);
            return false;
        }
    }
}