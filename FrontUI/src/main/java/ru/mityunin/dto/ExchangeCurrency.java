package ru.mityunin.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeCurrency {
    private Long id;
    private LocalDateTime localDateTime;
    private CurrencyType currencyFrom;
    private CurrencyType currencyTo;
    private int value;
}
