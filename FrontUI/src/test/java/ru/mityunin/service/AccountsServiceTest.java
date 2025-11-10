package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.*;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AuthenticatedRestTemplateService restTemplateHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AccountsService accountsService;

    @BeforeEach
    void setUp() {
        accountsService = new AccountsService("http://localhost:8080", restTemplateHelper);
        ReflectionTestUtils.setField(accountsService, "apiAccounts", "/accounts-service");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void authenticate_Success() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testuser");
        authRequest.setPassword("password");

        UserDto expectedUser = new UserDto();
        expectedUser.setLogin("testuser");
        expectedUser.setFirstName("John");

        ApiResponse<UserDto> apiResponse = new ApiResponse<>(true, "Success", expectedUser);
        when(restTemplateHelper.postForApiResponse(anyString(), any(AuthRequest.class), eq(UserDto.class)))
                .thenReturn(apiResponse);

        // Act
        UserDto result = accountsService.authenticate("testuser", "password");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("John", result.getFirstName());
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/accounts-service/api/auth",
                authRequest,
                UserDto.class
        );
    }

    @Test
    void getUserByLogin_Success() {
        // Arrange
        UserDto expectedUser = new UserDto();
        expectedUser.setLogin("testuser");

        PaymentAccountDto account1 = new PaymentAccountDto();
        account1.setAccountNumber("ACC002");
        account1.setBalance(new BigDecimal("500.00"));

        PaymentAccountDto account2 = new PaymentAccountDto();
        account2.setAccountNumber("ACC001");
        account2.setBalance(new BigDecimal("1000.00"));

        expectedUser.setPaymentAccounts(Arrays.asList(account1, account2));

        ApiResponse<UserDto> apiResponse = new ApiResponse<>(true, "Success", expectedUser);
        when(restTemplateHelper.getForApiResponse(anyString(), eq(UserDto.class)))
                .thenReturn(apiResponse);

        // Act
        UserDto result = accountsService.getUserByLogin("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        // Проверяем, что счета отсортированы по номеру
        assertEquals("ACC001", result.getPaymentAccounts().get(0).getAccountNumber());
        assertEquals("ACC002", result.getPaymentAccounts().get(1).getAccountNumber());
        verify(restTemplateHelper).getForApiResponse(
                "http://localhost:8080/accounts-service/api/testuser",
                UserDto.class
        );
    }

    @Test
    void getAllUsersExcept_Success() {
        // Arrange
        UserDto[] usersArray = new UserDto[]{
                createUserDto("user1"),
                createUserDto("user2")
        };

        ApiResponse<UserDto[]> apiResponse = new ApiResponse<>(true, "Success", usersArray);
        when(restTemplateHelper.getForApiResponse(anyString(), eq(UserDto[].class)))
                .thenReturn(apiResponse);

        // Act
        ApiResponse<List<UserDto>> result = accountsService.getAllUsersExcept("currentuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getMessage());
        assertEquals(2, result.getData().size());
        verify(restTemplateHelper).getForApiResponse(
                "http://localhost:8080/accounts-service/api/findAllExcept/currentuser",
                UserDto[].class
        );
    }

    @Test
    void getAllUsersExcept_NullData_ReturnsEmptyList() {
        // Arrange
        ApiResponse<UserDto[]> apiResponse = new ApiResponse<>(true, "Success", null);
        when(restTemplateHelper.getForApiResponse(anyString(), eq(UserDto[].class)))
                .thenReturn(apiResponse);

        // Act
        ApiResponse<List<UserDto>> result = accountsService.getAllUsersExcept("currentuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    void paymentAccountsByLogin_Success() {
        // Arrange
        PaymentAccountDto[] accountsArray = new PaymentAccountDto[]{
                createPaymentAccount("ACC001"),
                createPaymentAccount("ACC002")
        };

        ApiResponse<PaymentAccountDto[]> apiResponse = new ApiResponse<>(true, "Success", accountsArray);
        when(restTemplateHelper.getForApiResponse(anyString(), eq(PaymentAccountDto[].class)))
                .thenReturn(apiResponse);

        // Act
        ApiResponse<List<PaymentAccountDto>> result = accountsService.paymentAccountsByLogin("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(restTemplateHelper).getForApiResponse(
                "http://localhost:8080/accounts-service/api/paymentAccounts/testuser",
                PaymentAccountDto[].class
        );
    }

    @Test
    void registerUser_Success() {
        // Arrange
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setLogin("newuser");
        registrationRequest.setPassword("password");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("john@example.com");
        registrationRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserDto expectedUser = new UserDto();
        expectedUser.setLogin("newuser");

        ApiResponse<UserDto> apiResponse = new ApiResponse<>(true, "Success", expectedUser);
        when(restTemplateHelper.postForApiResponse(anyString(), any(UserRegistrationRequest.class), eq(UserDto.class)))
                .thenReturn(apiResponse);

        // Act
        UserDto result = accountsService.registerUser(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getLogin());
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/accounts-service/api/register",
                registrationRequest,
                UserDto.class
        );
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "User deleted", null);
        when(restTemplateHelper.postForApiResponse(anyString(), eq("testuser"), eq(Void.class)))
                .thenReturn(apiResponse);

        // Act
        ApiResponse<Void> result = accountsService.deleteUser("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("User deleted", result.getMessage());
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/accounts-service/api/delete",
                "testuser",
                Void.class
        );
    }

    @Test
    void updatePassword_Success() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testuser");
        authRequest.setPassword("newpassword");

        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Password updated", null);
        when(restTemplateHelper.postForApiResponse(anyString(), any(AuthRequest.class), eq(Void.class)))
                .thenReturn(apiResponse);

        // Act
        boolean result = accountsService.updatePassword(authRequest);

        // Assert
        assertTrue(result);
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/accounts-service/api/update/password",
                authRequest,
                Void.class
        );
    }

    @Test
    void updateUserInfo_Success() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setLogin("testuser");
        userDto.setFirstName("UpdatedName");

        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "User updated", null);
        when(restTemplateHelper.postForApiResponse(anyString(), any(UserDto.class), eq(Void.class)))
                .thenReturn(apiResponse);

        // Act
        boolean result = accountsService.updateUserInfo(userDto);

        // Assert
        assertTrue(result);
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/accounts-service/api/update/userInfo",
                userDto,
                Void.class
        );
    }

    // Вспомогательные методы
    private UserDto createUserDto(String login) {
        UserDto user = new UserDto();
        user.setLogin(login);
        user.setFirstName("First" + login);
        user.setLastName("Last" + login);
        return user;
    }

    private PaymentAccountDto createPaymentAccount(String accountNumber) {
        PaymentAccountDto account = new PaymentAccountDto();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCurrency("USD");
        return account;
    }
}