package ru.mityunin;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;

@Service
public class AuthenticatedRestTemplateService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticatedRestTemplateService.class);
    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private RestTemplateHelper restTemplateHelper;

    @Value("${client.registration.id}")
    private String clientRegistrationId;


    @Retry(name = "externalServiceRetry", fallbackMethod = "fallbackMethod")
    @CircuitBreaker(name = "externalServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public <T> ApiResponse<T> getForApiResponse(String url, Class<T> responseType) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId) // или другой сервис
                .principal("service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();
            // Добавляем токен в заголовок и делаем запрос
            return restTemplateHelper.getForApiResponse(url, responseType, token);
        }

        return ApiResponse.error("Failed to obtain access token");
    }

    @Retry(name = "externalServiceRetry", fallbackMethod = "fallbackMethod")
    @CircuitBreaker(name = "externalServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public <T> ApiResponse<T> postForApiResponse(String url, Object request, Class<T> responseType) {
        log.info("[AuthenticatedRestTemplateService] url: {}, request: {} ", url, request);
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId) // или другой сервис
                .principal("service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();
            // Добавляем токен в заголовок и делаем запрос
            return restTemplateHelper.postForApiResponse(url, request, responseType, token);
        }

        return ApiResponse.error("Failed to obtain access token");
    }


    // Общий Fallback метод
    // Важно: Сигнатура должна совпадать с оригинальным методом + последний параметр - Exception
    private <T> ApiResponse<T> fallbackMethod(String url, Object request, Class<T> responseType, Exception e) {
        // Логируем ошибку
        log.error("Fallback triggered for request to {}. Reason: {}", url, e.getMessage());

        // Возвращаем заглушку
        // Можно создать разные заглушки для разных responseType, если необходимо
        return ApiResponse.error("Service temporarily unavailable. Fallback response. Reason: " + e.getMessage());
    }

    // Перегруженный fallback для GET метода (у него нет параметра 'request')
    private <T> ApiResponse<T> fallbackMethod(String url, Class<T> responseType, Exception e) {
        log.error("Fallback triggered for GET request to {}. Reason: {}", url, e.getMessage());
        return ApiResponse.error("Service temporarily unavailable. Fallback response. Reason: " + e.getMessage());
    }
}
