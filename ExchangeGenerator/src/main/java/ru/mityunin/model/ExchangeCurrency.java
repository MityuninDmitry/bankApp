package ru.mityunin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exchange_currency", schema = "exchange_generator")
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    @Column(name = "currency_from")
    private CurrencyType currencyFrom;

    @Column(name = "currency_to")
    private CurrencyType currencyTo;

    @Column(name = "\"value\"")
    private int value;
}
