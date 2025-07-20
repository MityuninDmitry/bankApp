package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.UserDto;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final RestTemplate restTemplate;
    private final String serviceUrl;

    public CashService(RestTemplate restTemplate,
                           @Value("${service.cash.url}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public boolean processOperation(CashOperationRequest cashOperationRequest) {
        log.info("Cash operation request {}", cashOperationRequest);
        String url = serviceUrl + "/cash/processOperation";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(cashOperationRequest, headers),
                    Boolean.class
            );

            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {

            return false;
        }

    }
}
