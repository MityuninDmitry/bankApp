package ru.mityunin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;

@Data
public class CashOperationRequest {
    @NotBlank(message = "AccountNumber is required")
    private String accountNumber;
    @NotBlank(message = "Action with money[DEPOSIT, WITHDRAWN] is required")
    private CashOperation action;
    @NotNull(message = "Money cannot be null")
    @DecimalMin(value = "0.0", message = "Money must be positive")
    private BigDecimal money;
}
