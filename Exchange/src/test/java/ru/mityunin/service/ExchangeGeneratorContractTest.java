package ru.mityunin.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.ExchangeCurrencyFrontUIDto;
import ru.mityunin.model.CurrencyType;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        ids = "ru.mityunin:ExchangeGenerator:0.0.1-SNAPSHOT:stubs:8080",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ActiveProfiles("test")
public class ExchangeGeneratorContractTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExchangeService exchangeService;

    @Test
    void shouldReturnActualCurrencies() {
        // When
        ApiResponse<List<ExchangeCurrencyDto>> response = exchangeService.actualCurrencies();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Актуальные курсы валют");
        assertThat(response.getData()).isNotNull();


        // Проверяем структуру данных
        ExchangeCurrencyDto firstCurrency = response.getData().get(0);
        assertThat(firstCurrency.getLocalDateTime()).isNotNull();
        assertThat(firstCurrency.getCurrencyFrom()).isNotNull();
        assertThat(firstCurrency.getCurrencyTo()).isNotNull();
        assertThat(firstCurrency.getValue()).isPositive();

        // Проверяем допустимые значения валют
        assertThat(firstCurrency.getCurrencyFrom())
                .isIn(CurrencyType.RUB, CurrencyType.USD, CurrencyType.CNY);
        assertThat(firstCurrency.getCurrencyTo())
                .isIn(CurrencyType.RUB, CurrencyType.USD, CurrencyType.CNY);
    }

    @Test
    void shouldReturnActualCurrenciesFrontUI() {
        // When
        ApiResponse<List<ExchangeCurrencyFrontUIDto>> response = exchangeService.actualCurrenciesFrontUI();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Актуальные курсы валют");
        assertThat(response.getData()).isNotNull();


        // Проверяем структуру данных для FrontUI
        ExchangeCurrencyFrontUIDto firstCurrency = response.getData().get(0);
        assertThat(firstCurrency.getCurrency()).isNotNull();
        assertThat(firstCurrency.getBuyPrice()).isNotNegative();
        assertThat(firstCurrency.getSellPrice()).isNotNegative();
    }

}
