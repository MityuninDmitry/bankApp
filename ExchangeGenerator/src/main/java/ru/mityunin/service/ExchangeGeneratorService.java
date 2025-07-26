package ru.mityunin.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.ExchangeCurrency;
import ru.mityunin.repository.ExchangeCurrencyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExchangeGeneratorService {
    private ExchangeCurrencyRepository exchangeCurrencyRepository;

    public ExchangeGeneratorService(ExchangeCurrencyRepository exchangeCurrencyRepository) {
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
    }

    // Генерация данных каждую секунду (1000 мс)
    @Scheduled(fixedRate = 60000)  // fixedRate = 1 секунда (1000 мс)
    @Transactional
    public void generateCurrency() {
        exchangeCurrencyRepository.deleteAll();
        LocalDateTime localDateTime = LocalDateTime.now();
        for (CurrencyType currencyFrom : CurrencyType.values()) {
            for (CurrencyType currencyTo : CurrencyType.values()) {
                if (!currencyFrom.equals(currencyTo) && !currencyFrom.equals(CurrencyType.RUB)) {
                    ExchangeCurrency FROM_TO = new ExchangeCurrency();
                    FROM_TO.setLocalDateTime(localDateTime);
                    FROM_TO.setCurrencyFrom(currencyFrom);
                    FROM_TO.setCurrencyTo(currencyTo);
                    FROM_TO.setValue(getRandomValue());
                    exchangeCurrencyRepository.save(FROM_TO);
                }

            }
        }

        ExchangeCurrency RUB_RUB = new ExchangeCurrency();
        RUB_RUB.setCurrencyFrom(CurrencyType.RUB);
        RUB_RUB.setCurrencyTo(CurrencyType.RUB);
        RUB_RUB.setValue(1);
        RUB_RUB.setLocalDateTime(localDateTime);
        exchangeCurrencyRepository.save(RUB_RUB);

    }

    public ApiResponse<ExchangeCurrency> getLastCurrencyBy(CurrencyType currencyFrom, CurrencyType currencyTo) {
         List<ExchangeCurrency> exchangeCurrencyList = exchangeCurrencyRepository.findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(currencyFrom,currencyTo);
         if (exchangeCurrencyList.isEmpty()) {
             return ApiResponse.error("Не найден курс валют для" + currencyFrom.toString() + " и " + currencyTo.toString());
         } else {
             return ApiResponse.success("Найден курс валют",exchangeCurrencyList.getLast());
         }

    }

    public ApiResponse<List<ExchangeCurrency>> actualCurrencies() {
        LocalDateTime localDateTime = exchangeCurrencyRepository.findLatestDateTime();
        List<ExchangeCurrency> exchangeCurrencyList = exchangeCurrencyRepository.findByLocalDateTime(localDateTime);
        return ApiResponse.success("Актуальные курсы валют", exchangeCurrencyList);
    }

    private int getRandomValue() {
       return ThreadLocalRandom.current().nextInt(50, 101);
    }


}
