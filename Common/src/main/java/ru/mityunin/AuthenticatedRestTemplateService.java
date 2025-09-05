package ru.mityunin;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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

import java.util.function.Supplier;

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

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public AuthenticatedRestTemplateService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalServiceCircuitBreaker");
        this.retry = retryRegistry.retry("externalServiceRetry");
    }

    public <T> ApiResponse<T> getForApiResponse(String url, Class<T> responseType) {
        try {
            Supplier<ApiResponse<T>> supplier = () -> executeGetRequest(url, responseType); // вызов сервиса
            Supplier<ApiResponse<T>> retryableSupplier = Retry.decorateSupplier(retry, supplier); // декоратор ретрая над вызовом
            Supplier<ApiResponse<T>> circuitBreakerSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, retryableSupplier); // сиркут бреакер над ретраеями

            return circuitBreakerSupplier.get();
        } catch (Exception e) {
            return fallbackMethod(url, responseType, e);
        }
    }

    public <T> ApiResponse<T> postForApiResponse(String url, Object request, Class<T> responseType) {
        log.info("[AuthenticatedRestTemplateService] url: {}, request: {} ", url, request);
        try {
            Supplier<ApiResponse<T>> supplier = () -> executePostRequest(url, request, responseType);
            Supplier<ApiResponse<T>> retryableSupplier = Retry.decorateSupplier(retry, supplier);
            Supplier<ApiResponse<T>> circuitBreakerSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, retryableSupplier);

            return circuitBreakerSupplier.get();
        } catch (Exception e) {
            return fallbackMethod(url, request, responseType, e);
        }
    }

    private <T> ApiResponse<T> executeGetRequest(String url, Class<T> responseType) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();
            return restTemplateHelper.getForApiResponse(url, responseType, token);
        }

        return ApiResponse.error("Failed to obtain access token");
    }

    private <T> ApiResponse<T> executePostRequest(String url, Object request, Class<T> responseType) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();
            return restTemplateHelper.postForApiResponse(url, request, responseType, token);
        }

        return ApiResponse.error("Failed to obtain access token");
    }

    // Fallback методы
    private <T> ApiResponse<T> fallbackMethod(String url, Object request, Class<T> responseType, Exception e) {
        log.error("Fallback triggered for request to {}. Reason: {}", url, e.getMessage());
        return ApiResponse.error("Service temporarily unavailable. Fallback response. Reason: " + e.getMessage());
    }

    private <T> ApiResponse<T> fallbackMethod(String url, Class<T> responseType, Exception e) {
        log.error("Fallback triggered for GET request to {}. Reason: {}", url, e.getMessage());
        return ApiResponse.error("Service temporarily unavailable. Fallback response. Reason: " + e.getMessage());
    }
}