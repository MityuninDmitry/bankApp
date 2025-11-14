package ru.mityunin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.*;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.service.AccountService;
import ru.mityunin.service.NotificationService;
import ru.mityunin.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserDto testUserDto;
    private PaymentAccountDto testPaymentAccountDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testUser");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));

        testUserDto = new UserDto();
        testUserDto.setLogin("testUser");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john@example.com");
        testUserDto.setBirthDate(LocalDate.of(1990, 1, 1));

        testPaymentAccountDto = new PaymentAccountDto();
        testPaymentAccountDto.setAccountNumber("ACC123456");
        testPaymentAccountDto.setCurrency(CurrencyType.RUB);
        testPaymentAccountDto.setBalance(BigDecimal.valueOf(1000.00));
        testPaymentAccountDto.setIsDeleted(false);
    }

    @Test
    void getUserByLogin_UserExists_ReturnsUser() throws Exception {
        when(userService.findByLogin("testUser")).thenReturn(testUser);
        when(userMapper.userToUserDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(get("/api/testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("USER FOUND"))
                .andExpect(jsonPath("$.data.login").value("testUser"));

        verify(userService).findByLogin("testUser");
        verify(userMapper).userToUserDto(testUser);
    }

    @Test
    void getUserByLogin_UserNotFound_ReturnsNotFound() throws Exception {
        when(userService.findByLogin("unknown")).thenReturn(null);

        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("NOT FOUND"));

        verify(userService).findByLogin("unknown");
        verify(userMapper, never()).userToUserDto(any());
    }

    @Test
    void registerUser_Success_ReturnsRegisteredUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setLogin("newUser");
        request.setPassword("password");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setBirthDate(LocalDate.of(1995, 5, 5));

        when(userService.findByLogin("newUser")).thenReturn(null);
        when(userMapper.registrationRequestToUser(request)).thenReturn(testUser);
        when(accountService.createDefaultAccounts(testUser)).thenReturn(Collections.emptyList());
        when(userService.saveUser(testUser)).thenReturn(testUser);
        when(userMapper.userToUserDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("REGISTERED"));

        verify(userService).findByLogin("newUser");
        verify(userMapper).registrationRequestToUser(request);
        verify(accountService).createDefaultAccounts(testUser);
        verify(userService).saveUser(testUser);
        verify(notificationService).sendNotification("testUser", "Успешно зарегистрирован");
    }

    @Test
    void registerUser_UserExists_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setLogin("existingUser");

        when(userService.findByLogin("existingUser")).thenReturn(testUser);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("SUCH USER EXISTS"));

        verify(userService).findByLogin("existingUser");
        verify(userMapper, never()).registrationRequestToUser(any());
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUser() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testUser");
        authRequest.setPassword("password");

        when(userService.findByLogin("testUser")).thenReturn(testUser);
        when(userService.checkPassword(testUser, "password")).thenReturn(true);
        when(userMapper.userToUserDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("GOOD"));

        verify(userService).findByLogin("testUser");
        verify(userService).checkPassword(testUser, "password");
        verify(userMapper).userToUserDto(testUser);
    }

    @Test
    void authenticateUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testUser");
        authRequest.setPassword("wrongPassword");

        when(userService.findByLogin("testUser")).thenReturn(testUser);
        when(userService.checkPassword(testUser, "wrongPassword")).thenReturn(false);

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("NO USER WITH LOGINtestUser"));

        verify(userService).findByLogin("testUser");
        verify(userService).checkPassword(testUser, "wrongPassword");
        verify(userMapper, never()).userToUserDto(any());
    }

    @Test
    void deleteUser_UserExists_ReturnsSuccess() throws Exception {
        when(userService.findByLogin("testUser")).thenReturn(testUser);
        when(userService.deleteUser("testUser")).thenReturn(ApiResponse.success("Успех удаления аккаунта"));

        mockMvc.perform(post("/api/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успех удаления аккаунта"));

        verify(userService).findByLogin("testUser");
        verify(userService).deleteUser("testUser");
    }

    @Test
    void deleteUser_UserNotFound_ReturnsBadRequest() throws Exception {
        when(userService.findByLogin("unknown")).thenReturn(null);

        mockMvc.perform(post("/api/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("NO SUCH USER"));

        verify(userService).findByLogin("unknown");
        verify(userService, never()).deleteUser(anyString());
    }

    @Test
    void updateUserPassword_Success_ReturnsOk() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testUser");
        authRequest.setPassword("newPassword");

        doNothing().when(userService).updateUserPassword(authRequest);

        mockMvc.perform(post("/api/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password updated"));

        verify(userService).updateUserPassword(authRequest);
        verify(notificationService).sendNotification("testUser", "Пароль обновлен!");
    }

    @Test
    void deletePaymentAccount_Success_ReturnsOk() throws Exception {
        PaymentAccountWithLoginDto requestDto = new PaymentAccountWithLoginDto();
        requestDto.setLogin("testUser");
        requestDto.setAccountNumber("ACC123456");

        when(accountService.deletePaymentAccount("ACC123456")).thenReturn(true);

        mockMvc.perform(post("/api/delete/paymentAccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SUCCESS DELETING"));

        verify(accountService).deletePaymentAccount("ACC123456");
        verify(notificationService).sendNotification("testUser", "Счет удален");
    }

    @Test
    void deletePaymentAccount_EmptyAccountNumber_ReturnsBadRequest() throws Exception {
        PaymentAccountWithLoginDto requestDto = new PaymentAccountWithLoginDto();
        requestDto.setLogin("testUser");
        requestDto.setAccountNumber("");

        mockMvc.perform(post("/api/delete/paymentAccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Attempt to delete account with empty account number"));

        verify(accountService, never()).deletePaymentAccount(anyString());
        verify(notificationService).sendNotification("testUser", "Счет : ошибка удаления");
    }

    @Test
    void getUsersExceptLogin_UsersFound_ReturnsUsers() throws Exception {
        List<UserDto> users = Arrays.asList(testUserDto);
        ApiResponse<List<UserDto>> response = ApiResponse.success("List of users, except testUser", users);

        when(userService.findAllExceptLogin("testUser")).thenReturn(response);

        mockMvc.perform(get("/api/findAllExcept/testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].login").value("testUser"));

        verify(userService).findAllExceptLogin("testUser");
    }

    @Test
    void getUsersExceptLogin_NoUsersFound_ReturnsNotFound() throws Exception {
        ApiResponse<List<UserDto>> response = ApiResponse.error("NOT FOUND");

        when(userService.findAllExceptLogin("testUser")).thenReturn(response);

        mockMvc.perform(get("/api/findAllExcept/testUser"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService).findAllExceptLogin("testUser");
    }
}