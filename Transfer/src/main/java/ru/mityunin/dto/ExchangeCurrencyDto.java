package ru.mityunin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mityunin.model.CurrencyType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCurrencyDto {
    private LocalDateTime localDateTime;
    private CurrencyType currencyFrom;
    private CurrencyType currencyTo;
    private int value;
}
