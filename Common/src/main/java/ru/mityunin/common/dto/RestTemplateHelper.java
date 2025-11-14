package ru.mityunin.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mityunin.AuthenticatedRestTemplateService;

@Component
public class RestTemplateHelper {
    private static final Logger log = LoggerFactory.getLogger(RestTemplateHelper.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestTemplateHelper(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> ApiResponse<T> postForApiResponse(String url, Object request, Class<T> responseType, String token) {
        log.info("🔄 Making call to: {}", url); // ← Добавьте это
        try {
            HttpHeaders headers = getJsonHeaders();
            headers.setBearerAuth(token); // Добавляем Bearer токен

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    String.class
            );
            log.info("[RestTemplateHelper] GOOD response: {}",response);
            return parseResponse(response.getBody(), responseType);
        } catch (HttpStatusCodeException e) {
            log.info("⚠️ HTTP error {} for: {}", e.getStatusCode(), url);
            return handleHttpStatusCodeException(e, url, responseType);
        } catch (RestClientException e) {
            log.info("🌐 Network error for: {}", url);
            log.error("[RestTemplateHelper] error RestClientException");
            throw new RuntimeException("Network error calling: " + url, e);
        } catch (Exception e) {
            log.info("❌ Unexpected error for: {}", url);
            throw new RuntimeException("Unexpected error calling: " + url, e);
        }
    }

    public <T> ApiResponse<T> getForApiResponse(String url, Class<T> responseType, String token) {
        try {
            HttpHeaders headers = getJsonHeaders();
            headers.setBearerAuth(token); // Добавляем Bearer токен


            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getJsonHeaders(), headers),
                    String.class
            );

            return parseResponse(response.getBody(), responseType);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e, url, responseType);
        } catch (RestClientException e) {
            // Сетевые ошибки - ПРОБРАСЫВАЕМ для Resilience4j
            throw new RuntimeException("Network error calling: " + url, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error calling: " + url, e);
        }
    }

    // GET-запрос с возвратом ApiResponse
    public <T> ApiResponse<T> getForApiResponse(String url, Class<T> responseType) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getJsonHeaders()),
                    String.class
            );
            return parseResponse(response.getBody(), responseType);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e, url, responseType);
        } catch (RestClientException e) {
            // Сетевые ошибки - ПРОБРАСЫВАЕМ для Resilience4j
            throw new RuntimeException("Network error calling: " + url, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error calling: " + url, e);
        }
    }

    public <T> ApiResponse<T> postForApiResponse(String url, Object request, Class<T> responseType) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request, getJsonHeaders()),
                    String.class
            );
            return parseResponse(response.getBody(), responseType);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e, url, responseType);
        } catch (RestClientException e) {
            // Сетевые ошибки - ПРОБРАСЫВАЕМ для Resilience4j
            throw new RuntimeException("Network error calling: " + url, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error calling: " + url, e);
        }
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> ApiResponse<T> parseResponse(String json, Class<T> responseType) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(root.path("success").asBoolean());
        response.setMessage(root.path("message").asText());

        JsonNode dataNode = root.path("data");
        if (!dataNode.isMissingNode()) {
            T data = objectMapper.treeToValue(dataNode, responseType);
            response.setData(data);
        }
        return response;
    }

    private <T> ApiResponse<T> parseErrorResponse(String errorJson, Class<T> responseType) {
        try {
            return parseResponse(errorJson, responseType);
        } catch (JsonProcessingException e) {
            return ApiResponse.error(errorJson);
        }
    }

    private <T> ApiResponse<T> handleHttpStatusCodeException(
            HttpStatusCodeException e,
            String url,
            Class<T> responseType) {

        if (e.getStatusCode().is5xxServerError()) {
            log.error("[RestTemplateHelper] 5xx Server Error ({}): {}", e.getStatusCode(), url);
            throw new RuntimeException("Server error " + e.getStatusCode() + " calling: " + url, e);
        } else {
            log.warn("[RestTemplateHelper] 4xx Client Error ({}): {}", e.getStatusCode(), url);
            return parseErrorResponse(e.getResponseBodyAsString(), responseType);
        }
    }
}