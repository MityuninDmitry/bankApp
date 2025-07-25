package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;

import java.util.Collections;
import java.util.Map;

@Service
public class AccountsService {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);
    private final RestTemplate restTemplate;
    private final String accountsServiceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public AccountsService(RestTemplate restTemplate,
                           @Value("${service.accounts.url}") String accountsServiceUrl, RestTemplateHelper restTemplateHelper) {
        this.restTemplate = restTemplate;
        this.accountsServiceUrl = accountsServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    // Метод для проверки логина/пароля
    public UserDto authenticate(String login, String password) {
        log.info("Authenticating user: {}", login);
        String url = accountsServiceUrl + "/accounts/auth";

        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(login);
        authRequest.setPassword(password);

        return restTemplateHelper.postForApiResponse(url, authRequest, UserDto.class).getData();
    }

    // Метод для получения данных пользователя без проверки пароля
    public UserDto getUserByLogin(String login) {
        log.info("Getting user by login: {}", login);
        String url = accountsServiceUrl + "/accounts/" + login;
        return restTemplateHelper.getForApiResponse(url,UserDto.class).getData();
    }

    public boolean deletePaymentAccount(String accountNumber) {
        log.info("Attempting to delete accountNumber: {}", accountNumber);
        String url = accountsServiceUrl + "/accounts/delete/paymentAccount";
        Map<String, String> requestBody = Collections.singletonMap("accountNumber", accountNumber);
        return restTemplateHelper.postForApiResponse(url,requestBody, Void.class).isSuccess();
    }

    public boolean addPaymentAccount(String accountNumber) {
        log.info("Attempting to delete accountNumber: {}", accountNumber);
        String url = accountsServiceUrl + "/accounts/add/paymentAccount";
        Map<String, String> requestBody = Collections.singletonMap("accountNumber", accountNumber);
        return restTemplateHelper.postForApiResponse(url,requestBody,Void.class).isSuccess();
    }

    public UserDto registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering new user: {}", registrationRequest.getLogin());
        String url = accountsServiceUrl + "/accounts/register";
        return restTemplateHelper.postForApiResponse(url,registrationRequest,UserDto.class).getData();
    }

    public boolean deleteUser(String login) {
        log.info("Deleting user: {}", login);
        String url = accountsServiceUrl + "/accounts/delete";

        return restTemplateHelper.postForApiResponse(url,login, Void.class).isSuccess();
    }

    public boolean updatePassword(AuthRequest authRequest) {
        log.info("Updating password for user: {}", authRequest.getLogin());
        String url = accountsServiceUrl + "/accounts/update/password";
        return restTemplateHelper.postForApiResponse(url, authRequest, Void.class).isSuccess();

    }

    public boolean updateUserInfo(UserDto userDto) {
        log.info("Updating user info for user: {}", userDto.getLogin());
        String url = accountsServiceUrl + "/accounts/update/userInfo";
        return restTemplateHelper.postForApiResponse(url,userDto, Void.class).isSuccess();

    }
}