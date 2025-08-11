package ru.mityunin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCurrencyFrontUIDto {
    private CurrencyType currency;
    private int buyPrice;
    private int sellPrice;
}
