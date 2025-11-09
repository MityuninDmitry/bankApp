package ru.mityunin;

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
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;
import ru.mityunin.service.CashService;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        ids = {"ru.mityunin:Accounts:0.0.1-SNAPSHOT:stubs:8080"},
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ActiveProfiles("test")
public class CashServiceContractTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CashService cashService;

    @Test
    public void shouldProcessOperationSuccessfully() {
        // Given
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setLogin("testUser");
        request.setAccountNumber("ACC_SUCCESS");
        request.setAction(CashOperation.WITHDRAWN);
        request.setMoney(BigDecimal.valueOf(5000));

        // When
        ApiResponse<Void> response = cashService.processOperation(request);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Успех");
    }


    @Test
    public void shouldReturnErrorWhenInsufficientFunds() {
        // Given
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setLogin("testUser");
        request.setAccountNumber("ACC_INSUFFICIENT");
        request.setAction(CashOperation.WITHDRAWN);
        request.setMoney(BigDecimal.valueOf(5000));  // Пытаемся снять больше чем есть


        // When
        ApiResponse<Void> response = cashService.processOperation(request);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Не достаточно денег");
    }

    @Test
    public void shouldProcessDepositOperationSuccessfully() {
        // Given
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setLogin("testUser");
        request.setAccountNumber("ACC789012");
        request.setAction(CashOperation.DEPOSIT);  // Пополнение
        request.setMoney(BigDecimal.valueOf(1000));

        // When
        ApiResponse<Void> response = cashService.processOperation(request);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Успех");
    }
}
