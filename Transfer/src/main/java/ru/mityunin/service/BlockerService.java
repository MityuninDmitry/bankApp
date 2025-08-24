package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;

@Service
public class BlockerService {
    private final String blockerServiceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public BlockerService(@Value("${service.url.blocker}") String blockerServiceUrl, RestTemplateHelper restTemplateHelper) {
        this.blockerServiceUrl = blockerServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> isBlockerOperation() {
        String blockerUrl = blockerServiceUrl + "/api/isBlockerOperation";
        ApiResponse<Void> suspiciousOperationResponse = restTemplateHelper.postForApiResponse(blockerUrl, null, Void.class);
        return suspiciousOperationResponse;
    }


}
