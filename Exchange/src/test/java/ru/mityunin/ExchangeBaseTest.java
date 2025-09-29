package ru.mityunin;


import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.service.ExchangeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext
@AutoConfigureMessageVerifier
@ActiveProfiles("test")
public class ExchangeBaseTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ExchangeService exchangeService;

    @BeforeEach
    public void setup() {
        // Мокируем ответ сервиса
        List<ExchangeCurrencyDto> mockData = List.of(
                new ExchangeCurrencyDto(LocalDateTime.parse("2024-01-15T10:30:00"), CurrencyType.RUB, CurrencyType.CNY, 67),
                new ExchangeCurrencyDto(LocalDateTime.parse("2024-01-15T10:30:00"), CurrencyType.RUB, CurrencyType.USD, 70),
                new ExchangeCurrencyDto(LocalDateTime.parse("2024-01-15T10:30:00"), CurrencyType.CNY, CurrencyType.RUB, 62),
                new ExchangeCurrencyDto(LocalDateTime.parse("2024-01-15T10:30:00"), CurrencyType.USD, CurrencyType.RUB, 100),
                new ExchangeCurrencyDto(LocalDateTime.parse("2024-01-15T10:30:00"), CurrencyType.RUB, CurrencyType.RUB, 1)
        );

        ApiResponse<List<ExchangeCurrencyDto>> mockResponse = new ApiResponse<>(
                true,
                "Актуальные курсы валют",
                mockData
        );

        when(exchangeService.actualCurrencies()).thenReturn(mockResponse);

        RestAssuredMockMvc.webAppContextSetup(context);
    }
}
