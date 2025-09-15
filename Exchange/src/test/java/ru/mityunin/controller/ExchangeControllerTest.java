package ru.mityunin.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.ExchangeCurrencyFrontUIDto;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.service.ExchangeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeControllerTest {

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private ExchangeController exchangeController;

    @Test
    void actualCurrencies_ShouldReturnOkResponseWithData() {

        List<ExchangeCurrencyDto> expectedData = List.of(
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.USD, CurrencyType.RUB, 75),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.CNY, CurrencyType.RUB, 85)
        );
        ApiResponse<List<ExchangeCurrencyDto>> mockResponse = new ApiResponse<>(true, "Success", expectedData);

        when(exchangeService.actualCurrencies()).thenReturn(mockResponse);


        ResponseEntity<ApiResponse<List<ExchangeCurrencyDto>>> response =
                exchangeController.actualCurrencies();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(expectedData, response.getBody().getData());

        verify(exchangeService, times(1)).actualCurrencies();
    }

    @Test
    void actualCurrenciesFrontUI_ShouldReturnOkResponseWithData() {

        List<ExchangeCurrencyFrontUIDto> expectedData = List.of(
                new ExchangeCurrencyFrontUIDto(CurrencyType.USD, 75, 74),
                new ExchangeCurrencyFrontUIDto(CurrencyType.CNY, 85, 84)
        );
        ApiResponse<List<ExchangeCurrencyFrontUIDto>> mockResponse =
                new ApiResponse<>(true, "Success", expectedData);

        when(exchangeService.actualCurrenciesFrontUI()).thenReturn(mockResponse);


        ResponseEntity<ApiResponse<List<ExchangeCurrencyFrontUIDto>>> response =
                exchangeController.actualCurrenciesFrontUI();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(expectedData, response.getBody().getData());

        verify(exchangeService, times(1)).actualCurrenciesFrontUI();
    }

    @Test
    void actualCurrencies_ShouldReturnOkResponseWhenServiceReturnsNullData() {

        ApiResponse<List<ExchangeCurrencyDto>> mockResponse = new ApiResponse<>(true, "Success", null);

        when(exchangeService.actualCurrencies()).thenReturn(mockResponse);


        ResponseEntity<ApiResponse<List<ExchangeCurrencyDto>>> response =
                exchangeController.actualCurrencies();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNull(response.getBody().getData());

        verify(exchangeService, times(1)).actualCurrencies();
    }
}