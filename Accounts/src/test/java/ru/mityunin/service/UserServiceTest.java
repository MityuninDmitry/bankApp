package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.UserDto;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private PaymentAccount testPaymentAccount;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));

        testPaymentAccount = new PaymentAccount();
        testPaymentAccount.setId(1L);
        testPaymentAccount.setAccountNumber("ACC123456");
        testPaymentAccount.setCurrency(CurrencyType.RUB);
        testPaymentAccount.setBalance(BigDecimal.valueOf(1000.00));
        testPaymentAccount.setIsDeleted(false);
        testPaymentAccount.setUser(testUser);

        testUser.setPaymentAccounts(Arrays.asList(testPaymentAccount));

        testUserDto = new UserDto();
        testUserDto.setLogin("testUser");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john@example.com");
        testUserDto.setBirthDate(LocalDate.of(1990, 1, 1));
    }

    @Test
    void findByLogin_UserExists_ReturnsUser() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        User result = userService.findByLogin("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getLogin());
        verify(userRepository).findByLogin("testUser");
    }

    @Test
    void findByLogin_UserNotExists_ReturnsNull() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        User result = userService.findByLogin("unknown");

        assertNull(result);
        verify(userRepository).findByLogin("unknown");
    }

    @Test
    void findAllExceptLogin_UsersExist_ReturnsUsersList() {
        User anotherUser = new User();
        anotherUser.setLogin("anotherUser");

        List<User> users = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.userToUserDto(testUser)).thenReturn(testUserDto);

        ApiResponse<List<UserDto>> response = userService.findAllExceptLogin("anotherUser");

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("testUser", response.getData().get(0).getLogin());
        verify(userRepository).findAll();
    }

    @Test
    void paymentAccountsByLogin_UserExists_ReturnsPaymentAccounts() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        PaymentAccountDto paymentAccountDto = new PaymentAccountDto();
        paymentAccountDto.setAccountNumber("ACC123456");
        when(userMapper.paymentAccountToPaymentAccountDto(testPaymentAccount)).thenReturn(paymentAccountDto);

        ApiResponse<List<PaymentAccountDto>> response = userService.paymentAccountsByLogin("testUser");

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("ACC123456", response.getData().get(0).getAccountNumber());
        verify(userRepository).findByLogin("testUser");
    }

    @Test
    void saveUser_EncodesPasswordAndSaves() {
        User newUser = new User();
        newUser.setLogin("newUser");
        newUser.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(newUser)).thenReturn(newUser);

        User result = userService.saveUser(newUser);

        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(newUser);
    }

    @Test
    void checkPassword_ValidPassword_ReturnsTrue() {
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        boolean result = userService.checkPassword(testUser, "password");

        assertTrue(result);
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void checkPassword_InvalidPassword_ReturnsFalse() {
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        boolean result = userService.checkPassword(testUser, "wrongPassword");

        assertFalse(result);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    void deleteUser_ZeroBalance_DeletesUser() {
        testPaymentAccount.setBalance(BigDecimal.ZERO);
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        ApiResponse<Void> response = userService.deleteUser("testUser");

        assertTrue(response.isSuccess());
        assertEquals("Успех удаления аккаунта", response.getMessage());
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_NonZeroBalance_ReturnsError() {
        testPaymentAccount.setBalance(BigDecimal.valueOf(100.00));
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        ApiResponse<Void> response = userService.deleteUser("testUser");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("имеет ненулевой баланс"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateUserPassword_UserExists_UpdatesPassword() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin("testUser");
        authRequest.setPassword("newPassword");

        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.updateUserPassword(authRequest);

        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(userRepository).findByLogin("testUser");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserInfo_UserExists_UpdatesUserInfo() {
        // Given
        PaymentAccountDto paymentAccountDto = new PaymentAccountDto();
        paymentAccountDto.setAccountNumber("ACC123456");
        paymentAccountDto.setCurrency(CurrencyType.USD);
        paymentAccountDto.setBalance(BigDecimal.valueOf(2000.00));
        paymentAccountDto.setIsDeleted(false);

        testUserDto.setPaymentAccounts(Arrays.asList(paymentAccountDto));
        testUserDto.setFirstName("UpdatedFirstName");
        testUserDto.setLastName("UpdatedLastName");

        // Используем изменяемый список
        testUser.setPaymentAccounts(new ArrayList<>(Arrays.asList(testPaymentAccount)));

        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.updateUserInfo(testUserDto);

        // Then
        assertEquals("UpdatedFirstName", testUser.getFirstName());
        assertEquals("UpdatedLastName", testUser.getLastName());
        verify(userRepository).findByLogin("testUser");
        verify(userRepository).save(testUser);

        // Убедитесь, что маппер НЕ вызывается для существующих счетов
        verify(userMapper, never()).paymentAccountDtoToPaymentAccount(any());
    }
}