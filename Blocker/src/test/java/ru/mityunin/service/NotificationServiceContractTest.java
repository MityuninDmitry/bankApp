package ru.mityunin.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import ru.mityunin.common.dto.ApiResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureStubRunner(
        ids = {"ru.mityunin:Notifications:+:stubs:8080"}
)
class NotificationServiceContractTest {

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