package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.mapper.ExchangeCurrencyMapper;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.ExchangeCurrency;
import ru.mityunin.repository.ExchangeCurrencyRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeGeneratorServiceTest {

    @Mock
    private ExchangeCurrencyRepository exchangeCurrencyRepository;

    @Mock
    private ExchangeCurrencyMapper exchangeCurrencyMapper;

    @InjectMocks
    private ExchangeGeneratorService exchangeGeneratorService;

    @Captor
    private ArgumentCaptor<ExchangeCurrency> exchangeCurrencyCaptor;

    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
    }

    @Test
    void generateCurrency_ShouldDeleteAllAndSaveCorrectCurrencies() {
        // Arrange
        when(exchangeCurrencyRepository.save(any(ExchangeCurrency.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        exchangeGeneratorService.generateCurrency();

        // Assert
        verify(exchangeCurrencyRepository, times(1)).deleteAll();

        // Проверяем сохранение RUB-RUB курса
        verify(exchangeCurrencyRepository, atLeastOnce()).save(argThat(exchange ->
                exchange.getCurrencyFrom() == CurrencyType.RUB &&
                        exchange.getCurrencyTo() == CurrencyType.RUB &&
                        exchange.getValue() == 1
        ));

        // Проверяем сохранение других валютных пар
        verify(exchangeCurrencyRepository, atLeast(CurrencyType.values().length * 2 - 3)).save(any());
    }

    @Test
    void getLastCurrencyBy_WhenCurrencyExists_ShouldReturnSuccess() {
        // Arrange
        CurrencyType from = CurrencyType.USD;
        CurrencyType to = CurrencyType.RUB;
        ExchangeCurrency expectedCurrency = new ExchangeCurrency();
        expectedCurrency.setCurrencyFrom(from);
        expectedCurrency.setCurrencyTo(to);
        expectedCurrency.setValue(75);
        expectedCurrency.setLocalDateTime(testDateTime);

        when(exchangeCurrencyRepository.findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(from, to))
                .thenReturn(Arrays.asList(expectedCurrency));

        // Act
        ApiResponse<ExchangeCurrency> result = exchangeGeneratorService.getLastCurrencyBy(from, to);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Найден курс валют ", result.getMessage());
        assertEquals(expectedCurrency, result.getData());
    }

    @Test
    void getLastCurrencyBy_WhenCurrencyNotFound_ShouldReturnError() {
        // Arrange
        CurrencyType from = CurrencyType.USD;
        CurrencyType to = CurrencyType.CNY;

        when(exchangeCurrencyRepository.findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(from, to))
                .thenReturn(Collections.emptyList());

        // Act
        ApiResponse<ExchangeCurrency> result = exchangeGeneratorService.getLastCurrencyBy(from, to);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Не найден курс валют для USD и CNY", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void actualCurrencies_WhenDataExists_ShouldReturnSuccess() {
        // Arrange
        LocalDateTime latestDateTime = testDateTime;
        ExchangeCurrency exchangeCurrency1 = new ExchangeCurrency();
        exchangeCurrency1.setCurrencyFrom(CurrencyType.USD);
        exchangeCurrency1.setCurrencyTo(CurrencyType.RUB);
        exchangeCurrency1.setValue(75);
        exchangeCurrency1.setLocalDateTime(latestDateTime);

        ExchangeCurrency exchangeCurrency2 = new ExchangeCurrency();
        exchangeCurrency2.setCurrencyFrom(CurrencyType.CNY);
        exchangeCurrency2.setCurrencyTo(CurrencyType.RUB);
        exchangeCurrency2.setValue(85);
        exchangeCurrency2.setLocalDateTime(latestDateTime);

        ExchangeCurrencyDto dto1 = new ExchangeCurrencyDto();
        ExchangeCurrencyDto dto2 = new ExchangeCurrencyDto();

        when(exchangeCurrencyRepository.findLatestDateTime()).thenReturn(latestDateTime);
        when(exchangeCurrencyRepository.findByLocalDateTime(latestDateTime))
                .thenReturn(Arrays.asList(exchangeCurrency1, exchangeCurrency2));
        when(exchangeCurrencyMapper.exchangeCurrencyToExchangeCurrencyDto(exchangeCurrency1))
                .thenReturn(dto1);
        when(exchangeCurrencyMapper.exchangeCurrencyToExchangeCurrencyDto(exchangeCurrency2))
                .thenReturn(dto2);

        // Act
        ApiResponse<List<ExchangeCurrencyDto>> result = exchangeGeneratorService.actualCurrencies();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Актуальные курсы валют ", result.getMessage());
        assertEquals(2, result.getData().size());
        assertTrue(result.getData().contains(dto1));
        assertTrue(result.getData().contains(dto2));
    }

    @Test
    void actualCurrencies_WhenNoData_ShouldReturnEmptyList() {
        // Arrange
        LocalDateTime latestDateTime = testDateTime;

        when(exchangeCurrencyRepository.findLatestDateTime()).thenReturn(latestDateTime);
        when(exchangeCurrencyRepository.findByLocalDateTime(latestDateTime))
                .thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ExchangeCurrencyDto>> result = exchangeGeneratorService.actualCurrencies();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Актуальные курсы валют ", result.getMessage());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void getLastCurrencyBy_WithSameCurrency_ShouldReturnErrorIfNotRUB() {
        // Arrange
        CurrencyType from = CurrencyType.USD;
        CurrencyType to = CurrencyType.USD;

        when(exchangeCurrencyRepository.findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(from, to))
                .thenReturn(Collections.emptyList());

        // Act
        ApiResponse<ExchangeCurrency> result = exchangeGeneratorService.getLastCurrencyBy(from, to);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Не найден курс валют для "));
    }

    @Test
    void getLastCurrencyBy_WithRUBtoRUB_ShouldReturnSuccess() {
        // Arrange
        CurrencyType from = CurrencyType.RUB;
        CurrencyType to = CurrencyType.RUB;
        ExchangeCurrency expectedCurrency = new ExchangeCurrency();
        expectedCurrency.setCurrencyFrom(from);
        expectedCurrency.setCurrencyTo(to);
        expectedCurrency.setValue(1);
        expectedCurrency.setLocalDateTime(testDateTime);

        when(exchangeCurrencyRepository.findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(from, to))
                .thenReturn(Arrays.asList(expectedCurrency));

        // Act
        ApiResponse<ExchangeCurrency> result = exchangeGeneratorService.getLastCurrencyBy(from, to);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(expectedCurrency, result.getData());
        assertEquals(1, result.getData().getValue());
    }
}