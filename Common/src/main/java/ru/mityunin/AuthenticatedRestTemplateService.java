package ru.mityunin;

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

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private RestTemplateHelper restTemplateHelper;

    @Value("${client.registration.id}")
    private String clientRegistrationId;

    public <T> ApiResponse<T> postForApiResponse(String url, Object request, Class<T> responseType) {
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
}
