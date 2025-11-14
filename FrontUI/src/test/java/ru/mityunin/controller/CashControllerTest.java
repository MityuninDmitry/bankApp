package ru.mityunin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;
import ru.mityunin.service.CashService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashControllerTest {

    @Mock
    private CashService cashService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private CashController cashController;

    private CashOperationRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        validRequestDto = new CashOperationRequestDto();
        validRequestDto.setAccountNumber("1234567890");
        validRequestDto.setAction(CashOperation.DEPOSIT);
        validRequestDto.setMoney(new BigDecimal("100.50"));
        validRequestDto.setLogin("testuser");
    }

    @Test
    void actionWithPaymentAccount_Success() {
        // Arrange
        ApiResponse<Void> successResponse = ApiResponse.success("Operation completed successfully");
        when(cashService.processOperation(validRequestDto)).thenReturn(successResponse);

        // Act
        String result = cashController.actionWithPaymentAccount(validRequestDto, redirectAttributes);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(cashService).processOperation(validRequestDto);
        verifyNoInteractions(redirectAttributes);
    }

    @Test
    void actionWithPaymentAccount_Error() {
        // Arrange
        String errorMessage = "Insufficient funds";
        ApiResponse<Void> errorResponse = ApiResponse.error(errorMessage);
        when(cashService.processOperation(validRequestDto)).thenReturn(errorResponse);

        // Act
        String result = cashController.actionWithPaymentAccount(validRequestDto, redirectAttributes);

        // Assert
        assertEquals("redirect:/frontui/home", result);
        verify(cashService).processOperation(validRequestDto);
        verify(redirectAttributes).addFlashAttribute("actionWithPaymentAccountError", errorMessage);
    }
}