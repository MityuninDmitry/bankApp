package ru.mityunin.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class RestTemplateHelper {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestTemplateHelper(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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
            return parseErrorResponse(e.getResponseBodyAsString(), responseType);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
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
            return parseErrorResponse(e.getResponseBodyAsString(), responseType);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
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
}