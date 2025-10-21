package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.*;

import java.util.*;

@Service
public class AccountsService {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);
    private final String accountsServiceUrl;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    @Value("${service.api.accounts}")
    private String apiAccounts;

    public AccountsService(
            @Value("${service.url.gateway}") String accountsServiceUrl,
            AuthenticatedRestTemplateService restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    // Метод для проверки логина/пароля
    public UserDto authenticate(String login, String password) {
        log.info("Authenticating user: {}", login);
        String url = accountsServiceUrl + apiAccounts + "/api/auth";

        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(login);
        authRequest.setPassword(password);

        return restTemplateHelper.postForApiResponse(url, authRequest, UserDto.class).getData();
    }

    // Метод для получения данных пользователя без проверки пароля
    public UserDto getUserByLogin(String login) {
        log.info("Getting user by login: {}", login);
        String url = accountsServiceUrl + apiAccounts + "/api/" + login;
        UserDto userDto = restTemplateHelper.getForApiResponse(url,UserDto.class).getData();
        userDto.getPaymentAccounts().sort(Comparator.comparing(PaymentAccountDto::getAccountNumber));
        return userDto;
    }

    public ApiResponse<List<UserDto>> getAllUsersExcept(String login) {
        log.info("[Front UI] AccountsService getAllUsersExcept: {}", login);
        String url = accountsServiceUrl + apiAccounts + "/api/findAllExcept/" + login;
        ApiResponse<UserDto[]> response = restTemplateHelper.getForApiResponse(url, UserDto[].class);
        log.info("[Front UI] AccountsService getAllUsersExcept response: {}", response);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }
    public ApiResponse<List<PaymentAccountDto>> paymentAccountsByLogin(String login) {
        String url = accountsServiceUrl + apiAccounts + "/api/paymentAccounts/" + login;
        ApiResponse<PaymentAccountDto[]> response = restTemplateHelper.getForApiResponse(url, PaymentAccountDto[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }


    public boolean deletePaymentAccount(String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        PaymentAccountWithLoginDto requestDto = new PaymentAccountWithLoginDto();
        requestDto.setAccountNumber(accountNumber);
        requestDto.setLogin(login);

        log.info("Attempting to delete accountNumber: {}", accountNumber);

        String url = accountsServiceUrl + apiAccounts + "/api/delete/paymentAccount";
        return restTemplateHelper.postForApiResponse(url,requestDto, Void.class).isSuccess();
    }

    public boolean addPaymentAccount(String accountNumber) {
        log.info("Attempting to delete accountNumber: {}", accountNumber);
        String url = accountsServiceUrl + apiAccounts + "/api/add/paymentAccount";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        PaymentAccountWithLoginDto requestDto = new PaymentAccountWithLoginDto();
        requestDto.setAccountNumber(accountNumber);
        requestDto.setLogin(login);

        return restTemplateHelper.postForApiResponse(url,requestDto,Void.class).isSuccess();
    }

    public UserDto registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering new user: {}", registrationRequest.getLogin());
        String url = accountsServiceUrl + apiAccounts + "/api/register";
        return restTemplateHelper.postForApiResponse(url,registrationRequest,UserDto.class).getData();
    }

    public ApiResponse<Void> deleteUser(String login) {
        log.info("Deleting user: {}", login);
        String url = accountsServiceUrl + apiAccounts + "/api/delete";

        return restTemplateHelper.postForApiResponse(url, login, Void.class);
    }

    public boolean updatePassword(AuthRequest authRequest) {
        log.info("Updating password for user: {}", authRequest.getLogin());
        String url = accountsServiceUrl + apiAccounts + "/api/update/password";
        return restTemplateHelper.postForApiResponse(url, authRequest, Void.class).isSuccess();

    }

    public boolean updateUserInfo(UserDto userDto) {
        log.info("Updating user info for user: {}", userDto.getLogin());
        String url = accountsServiceUrl + apiAccounts + "/api/update/userInfo";
        return restTemplateHelper.postForApiResponse(url,userDto, Void.class).isSuccess();

    }
}