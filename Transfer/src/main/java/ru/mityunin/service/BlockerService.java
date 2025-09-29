package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;

@Service
public class BlockerService {

    @Value("${service.api.blocker}")
    private String apiBlocker;
    private final String blockerServiceUrl;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    public BlockerService(
            @Value("${service.url.gateway}") String blockerServiceUrl,
            AuthenticatedRestTemplateService restTemplateHelper) {
        this.blockerServiceUrl = blockerServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> isBlockerOperation() {
        String blockerUrl = blockerServiceUrl + apiBlocker + "/api/isBlockerOperation";
        ApiResponse<Void> suspiciousOperationResponse = restTemplateHelper.postForApiResponse(blockerUrl, null, Void.class);
        return suspiciousOperationResponse;
    }


}
