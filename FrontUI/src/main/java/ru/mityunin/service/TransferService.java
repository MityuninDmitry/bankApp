package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.TransferRequestDto;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final String transferServiceUrl;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    @Value("${service.api.transfer}")
    private String apiTransfer;

    public TransferService(@Value("${service.url.gateway}") String transferServiceUrl, AuthenticatedRestTemplateService restTemplateHelper) {
        this.transferServiceUrl = transferServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> transferRequest(TransferRequestDto transferRequestDto) {
        log.info("Cash operation request {}", transferRequestDto);
        String url = transferServiceUrl + apiTransfer + "/api/transferRequest";
        return restTemplateHelper.postForApiResponse(url, transferRequestDto, Void.class);
    }
}
