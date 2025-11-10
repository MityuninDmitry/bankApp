package ru.mityunin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.service.AccountsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsControllerTest {

    @Mock
    private AccountsService accountsService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AccountsController accountsController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void deleteUser_Success() throws Exception {
        // Arrange
        String login = "testuser";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(login);

        ApiResponse<Void> deleteResponse = ApiResponse.success("User deleted");
        when(accountsService.deleteUser(login)).thenReturn(deleteResponse);

        SecurityContextLogoutHandler logoutHandler = mock(SecurityContextLogoutHandler.class);

        // Act
        String result = accountsController.deleteUser(authentication, request, response, null);

        // Assert
        assertEquals("redirect:/frontui/login?delete", result);
        verify(accountsService).deleteUser(login);
        verify(securityContext).setAuthentication(null);
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteUser_NotAuthenticated_RedirectsToLogin() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        String result = accountsController.deleteUser(authentication, request, response, null);

        // Assert
        assertEquals("redirect:/frontui/login?delete", result);
        verify(accountsService, never()).deleteUser(anyString());
    }

    @Test
    void deleteUser_ServiceReturnsError_AddsErrorAndRedirects() {
        // Arrange
        String login = "testuser";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(login);

        ApiResponse<Void> deleteResponse = ApiResponse.error("Delete failed");
        when(accountsService.deleteUser(login)).thenReturn(deleteResponse);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String result = accountsController.deleteUser(authentication, request, response, redirectAttributes);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(accountsService).deleteUser(login);
        verify(redirectAttributes).addFlashAttribute("deleteUserError", "Delete failed");
        verify(securityContext, never()).setAuthentication(null);
    }

    @Test
    void showRegistrationForm_ReturnsRegisterView() {
        // Act
        String result = accountsController.showRegistrationForm(model);

        // Assert
        assertEquals("register", result);
        verify(model).addAttribute(eq("registrationRequest"), any(UserRegistrationRequest.class));
    }


    @Test
    void registerUser_WithBindingErrors_ReturnsRegisterView() {
        // Arrange
        UserRegistrationRequest registrationRequest = createValidRegistrationRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = accountsController.registerUser(registrationRequest, bindingResult, model, request);

        // Assert
        assertEquals("register", result);
        verify(accountsService, never()).registerUser(any());
    }

    @Test
    void registerUser_RegistrationFails_AddsError() {
        // Arrange
        UserRegistrationRequest registrationRequest = createValidRegistrationRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(accountsService.registerUser(registrationRequest)).thenReturn(null);

        // Act
        String result = accountsController.registerUser(registrationRequest, bindingResult, model, request);

        // Assert
        assertEquals("register", result);
        verify(model).addAttribute("error", "Registration failed. Login might be already taken.");
        verify(accountsService).registerUser(registrationRequest);
    }

    @Test
    void updatePassword_Success() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testuser");
        authRequest.setPassword("newpassword");

        when(accountsService.updatePassword(authRequest)).thenReturn(true);

        // Act
        String result = accountsController.updatePassword(authRequest, model);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(accountsService).updatePassword(authRequest);
        verify(model).addAttribute("passwordUpdated", true);
    }

    @Test
    void updatePassword_Failure() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testuser");
        authRequest.setPassword("newpassword");

        when(accountsService.updatePassword(authRequest)).thenReturn(false);

        // Act
        String result = accountsController.updatePassword(authRequest, model);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(accountsService).updatePassword(authRequest);
        verify(model).addAttribute("passwordError", "Failed to update password");
    }

    @Test
    void updateUserInfo_Success() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setLogin("testuser");
        userDto.setFirstName("UpdatedName");

        when(accountsService.updateUserInfo(userDto)).thenReturn(true);

        // Act
        String result = accountsController.updateUserInfo(userDto, model);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(accountsService).updateUserInfo(userDto);
        verify(model).addAttribute("userInfoUpdated", true);
    }

    @Test
    void updateUserInfo_Failure() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setLogin("testuser");
        userDto.setFirstName("UpdatedName");

        when(accountsService.updateUserInfo(userDto)).thenReturn(false);

        // Act
        String result = accountsController.updateUserInfo(userDto, model);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(accountsService).updateUserInfo(userDto);
        verify(model).addAttribute("userInfoUpdatedError", "Failed to update user info");
    }

    @Test
    void paymentAccountsByLogin_Success() {
        // Arrange
        String login = "testuser";
        List<PaymentAccountDto> accounts = Arrays.asList(
                createPaymentAccount("ACC001"),
                createPaymentAccount("ACC002")
        );
        ApiResponse<List<PaymentAccountDto>> expectedResponse =
                new ApiResponse<>(true, "Success", accounts);

        when(accountsService.paymentAccountsByLogin(login)).thenReturn(expectedResponse);

        // Act
        ApiResponse<List<PaymentAccountDto>> result =
                accountsController.paymentAccountsByLogin(login);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(accountsService).paymentAccountsByLogin(login);
    }

    // Вспомогательные методы
    private UserRegistrationRequest createValidRegistrationRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setLogin("newuser");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1)); // Более 18 лет
        return request;
    }

    private PaymentAccountDto createPaymentAccount(String accountNumber) {
        PaymentAccountDto account = new PaymentAccountDto();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCurrency("USD");
        return account;
    }
}