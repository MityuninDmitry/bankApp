package ru.mityunin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.ExchangeCurrency;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExchangeCurrencyRepository extends JpaRepository<ExchangeCurrency, Long> {
    List<ExchangeCurrency> findByCurrencyFromAndCurrencyToOrderByLocalDateTimeAsc(
            CurrencyType currencyFrom,
            CurrencyType currencyTo
    );

    List<ExchangeCurrency> findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(
            CurrencyType currencyFrom,
            CurrencyType currencyTo
    );

    List<ExchangeCurrency> findByLocalDateTime(LocalDateTime localDateTime);

    // Находит самую последнюю дату из всех записей
    @Query("SELECT MAX(e.localDateTime) FROM ExchangeCurrency e")
    LocalDateTime findLatestDateTime();
}
