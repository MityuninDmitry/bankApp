package ru.mityunin.service;

import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BlockerService {
    public ApiResponse<Void> isSuspiciousOperation(CashOperationRequestDto cashOperationRequestDto) {
        if (cashOperationRequestDto.getAction().equals(CashOperation.WITHDRAWN)) {
            if (isSuspiciousValue(cashOperationRequestDto.getMoney())) {
                return ApiResponse.error("Подозрительная операция. Отклонено!");
            }
        }
        return ApiResponse.success("Все отлично. Работаем.");
    }

    public ApiResponse<Void> isSuspiciousOperation(BigDecimal value) {

        if (isSuspiciousValue(value)) {
            return ApiResponse.error("Подозрительная операция. Отклонено!");
        }
        return ApiResponse.success("Все отлично. Работаем.");
    }

    private boolean isSuspiciousValue(BigDecimal value) {
        if (value.compareTo(BigDecimal.valueOf(10_000)) > 0) {
            return true;
        }
        return false;
    }
}
