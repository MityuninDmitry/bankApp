package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.CashOperation;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.repository.PaymentAccountRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private PaymentAccountRepository paymentAccountRepository;

    @Mock
    private UserMapper userMapper;

    private AccountService accountService;

    private PaymentAccount testPaymentAccount;
    private User testUser;
    private PaymentAccountDto testPaymentAccountDto;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(paymentAccountRepository, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testUser");

        testPaymentAccount = new PaymentAccount();
        testPaymentAccount.setId(1L);
        testPaymentAccount.setAccountNumber("ACC123456");
        testPaymentAccount.setCurrency(CurrencyType.RUB);
        testPaymentAccount.setBalance(BigDecimal.valueOf(1000.00));
        testPaymentAccount.setIsDeleted(false);
        testPaymentAccount.setUser(testUser);

        testPaymentAccountDto = new PaymentAccountDto();
        testPaymentAccountDto.setAccountNumber("ACC123456");
        testPaymentAccountDto.setCurrency(CurrencyType.RUB);
        testPaymentAccountDto.setBalance(BigDecimal.valueOf(1000.00));
        testPaymentAccountDto.setIsDeleted(false);
    }

    @Test
    void deletePaymentAccount_AccountExistsWithZeroBalance_DeletesSuccessfully() {
        testPaymentAccount.setBalance(BigDecimal.ZERO);
        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);

        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);
        when(paymentAccountRepository.save(testPaymentAccount)).thenReturn(testPaymentAccount);

        boolean result = accountService.deletePaymentAccount("ACC123456");

        assertTrue(result);
        assertTrue(testPaymentAccount.getIsDeleted());
        verify(paymentAccountRepository).findByAccountNumber("ACC123456");
        verify(paymentAccountRepository).save(testPaymentAccount);
    }

    @Test
    void deletePaymentAccount_AccountExistsWithNonZeroBalance_ReturnsFalse() {
        testPaymentAccount.setBalance(BigDecimal.valueOf(100.00));
        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);

        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);

        boolean result = accountService.deletePaymentAccount("ACC123456");

        assertFalse(result);
        verify(paymentAccountRepository).findByAccountNumber("ACC123456");
        verify(paymentAccountRepository, never()).save(any());
    }

    @Test
    void deletePaymentAccount_AccountNotFound_ReturnsFalse() {
        when(paymentAccountRepository.findByAccountNumber("UNKNOWN")).thenReturn(Collections.emptyList());

        boolean result = accountService.deletePaymentAccount("UNKNOWN");

        assertFalse(result);
        verify(paymentAccountRepository).findByAccountNumber("UNKNOWN");
        verify(paymentAccountRepository, never()).save(any());
    }

    @Test
    void addPaymentAccount_AccountExists_ActivatesSuccessfully() {
        testPaymentAccount.setIsDeleted(true);
        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);

        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);
        when(paymentAccountRepository.save(testPaymentAccount)).thenReturn(testPaymentAccount);

        boolean result = accountService.addPaymentAccount("ACC123456");

        assertTrue(result);
        assertFalse(testPaymentAccount.getIsDeleted());
        verify(paymentAccountRepository).findByAccountNumber("ACC123456");
        verify(paymentAccountRepository).save(testPaymentAccount);
    }

    @Test
    void addPaymentAccount_AccountNotFound_ReturnsFalse() {
        when(paymentAccountRepository.findByAccountNumber("UNKNOWN")).thenReturn(Collections.emptyList());

        boolean result = accountService.addPaymentAccount("UNKNOWN");

        assertFalse(result);
        verify(paymentAccountRepository).findByAccountNumber("UNKNOWN");
        verify(paymentAccountRepository, never()).save(any());
    }

    @Test
    void createDefaultAccounts_CreatesThreeAccounts() {
        User user = new User();
        user.setLogin("newUser");

        List<PaymentAccount> result = accountService.createDefaultAccounts(user);

        assertEquals(3, result.size());

        assertEquals(CurrencyType.RUB, result.get(0).getCurrency());
        assertEquals(CurrencyType.CNY, result.get(1).getCurrency());
        assertEquals(CurrencyType.USD, result.get(2).getCurrency());

        assertTrue(result.get(0).getIsDeleted());
        assertTrue(result.get(1).getIsDeleted());
        assertTrue(result.get(2).getIsDeleted());

        assertEquals(user, result.get(0).getUser());
        assertEquals(user, result.get(1).getUser());
        assertEquals(user, result.get(2).getUser());
    }

    @Test
    void processOperation_Deposit_IncreasesBalance() {
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setAccountNumber("ACC123456");
        request.setAction(CashOperation.DEPOSIT);
        request.setMoney(BigDecimal.valueOf(500.00));

        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);
        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);
        when(paymentAccountRepository.save(testPaymentAccount)).thenReturn(testPaymentAccount);

        ApiResponse<Void> response = accountService.processOperation(request);

        assertTrue(response.isSuccess());
        assertEquals(BigDecimal.valueOf(1500.00), testPaymentAccount.getBalance());
        verify(paymentAccountRepository).save(testPaymentAccount);
    }

    @Test
    void processOperation_Withdraw_SufficientBalance_DecreasesBalance() {
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setAccountNumber("ACC123456");
        request.setAction(CashOperation.WITHDRAWN);
        request.setMoney(BigDecimal.valueOf(500.00));

        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);
        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);
        when(paymentAccountRepository.save(testPaymentAccount)).thenReturn(testPaymentAccount);

        ApiResponse<Void> response = accountService.processOperation(request);

        assertTrue(response.isSuccess());
        assertEquals(BigDecimal.valueOf(500.00), testPaymentAccount.getBalance());
        verify(paymentAccountRepository).save(testPaymentAccount);
    }

    @Test
    void processOperation_Withdraw_InsufficientBalance_ReturnsError() {
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setAccountNumber("ACC123456");
        request.setAction(CashOperation.WITHDRAWN);
        request.setMoney(BigDecimal.valueOf(1500.00));

        List<PaymentAccount> accounts = Arrays.asList(testPaymentAccount);
        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(accounts);

        ApiResponse<Void> response = accountService.processOperation(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Не достаточно денег"));
        assertEquals(BigDecimal.valueOf(1000.00), testPaymentAccount.getBalance());
        verify(paymentAccountRepository, never()).save(any());
    }

    @Test
    void getAccountInfo_ActiveAccount_ReturnsAccountInfo() {
        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(Arrays.asList(testPaymentAccount));
        when(userMapper.paymentAccountToPaymentAccountDto(testPaymentAccount)).thenReturn(testPaymentAccountDto);

        ApiResponse<PaymentAccountDto> response = accountService.getAccountInfo("ACC123456");

        assertTrue(response.isSuccess());
        assertEquals("ACC123456", response.getData().getAccountNumber());
        verify(paymentAccountRepository).findByAccountNumber("ACC123456");
        verify(userMapper).paymentAccountToPaymentAccountDto(testPaymentAccount);
    }

    @Test
    void getAccountInfo_DeletedAccount_ReturnsError() {
        testPaymentAccount.setIsDeleted(true);
        when(paymentAccountRepository.findByAccountNumber("ACC123456")).thenReturn(Arrays.asList(testPaymentAccount));

        ApiResponse<PaymentAccountDto> response = accountService.getAccountInfo("ACC123456");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("it's deleted"));
        verify(paymentAccountRepository).findByAccountNumber("ACC123456");
        verify(userMapper, never()).paymentAccountToPaymentAccountDto(any());
    }
}