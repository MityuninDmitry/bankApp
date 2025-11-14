package ru.mityunin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashOperationRequestDto {
    @NotBlank(message = "AccountNumber is required")
    private String accountNumber;
    @NotNull(message = "Action with money[DEPOSIT, WITHDRAWN] is required")
    private CashOperation action;
    @NotNull(message = "Money cannot be null")
    @DecimalMin(value = "0.0", message = "Money must be positive")
    private BigDecimal money;
    @NotBlank(message = "Login cannot be blank")
    private String login;
}
