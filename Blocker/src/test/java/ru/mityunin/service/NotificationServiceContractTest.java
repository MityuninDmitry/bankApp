package ru.mityunin.service;

import com.github.tomakehurst.wiremock.client.WireMock;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        ids = "ru.mityunin:Notifications:0.0.1-SNAPSHOT:stubs:8080",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ActiveProfiles("test")
class NotificationServiceContractTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NotificationService notificationService;

    @Test
    void sendNotification_ShouldCallExternalServiceWithCorrectContract() {
        // Act
        ApiResponse<Void> result = notificationService.sendNotification(
                "testUser",
                "Подозрительная операция. Повторите снова."
        );

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Успех добавления нотификации", result.getMessage());
    }
}