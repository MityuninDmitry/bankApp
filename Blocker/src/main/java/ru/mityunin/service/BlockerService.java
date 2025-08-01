package ru.mityunin.service;

import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.model.CashOperation;

@Service
public class BlockerService {

    public ApiResponse<Void> isSuspiciousOperation(CashOperationRequest cashOperationRequest) {
        if (cashOperationRequest.getAction().equals(CashOperation.WITHDRAWN)) {
            int result = (int) (Math.random() * 2);
            //result = 1; // FOR TEST PURPOSE ONLY
            if (result == 1) {
                return ApiResponse.error("Подозрительная операция. Отклонено!");
            }
        }
        return ApiResponse.success("Все отлично. Работаем.");
    }
}
