package ru.mityunin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.mityunin.model.CurrencyType;

import java.math.BigDecimal;

@Data
public class PaymentAccountDto {
    @PositiveOrZero(message = "Account number must be positive")
    private String accountNumber; // буду юзать guid для уникальности, чтоб не придумывать ничего сложного
    @NotBlank(message = "Currency is required")
    private CurrencyType currency;
    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.0", message = "Balance must be positive")
    private BigDecimal balance;
    private Boolean isDeleted;
}
