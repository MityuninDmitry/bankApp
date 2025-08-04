package ru.mityunin.mapper;


import org.mapstruct.Mapper;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.model.ExchangeCurrency;

@Mapper(componentModel = "spring")
public interface ExchangeCurrencyMapper {
    ExchangeCurrencyDto exchangeCurrencyToExchangeCurrencyDto(ExchangeCurrency user);
    ExchangeCurrency exchangeCurrencyDtoToExchangeCurrency(ExchangeCurrencyDto userDto);
}
