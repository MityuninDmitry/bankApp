package ru.mityunin.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class Resilience4jAutoConfiguration {

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(80) // Порог процента ошибок
                .slidingWindowSize(5) // Размер скользящего окна
                .minimumNumberOfCalls(5) // Минимальное количество вызовов
                .waitDurationInOpenState(Duration.ofSeconds(1)) // Время в OPEN состоянии
                .permittedNumberOfCallsInHalfOpenState(3) // Кол-во пробных вызовов в HALF_OPEN
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultCircuitBreakerConfig) {
        return CircuitBreakerRegistry.of(defaultCircuitBreakerConfig);
    }

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(5) // Максимальное количество попыток
                .waitDuration(Duration.ofSeconds(2)) // Задержка между попытками
                .retryExceptions(
                        IOException.class,
                        TimeoutException.class,
                        RestClientException.class,
                        HttpServerErrorException.class,
                        RuntimeException.class
                )
                .failAfterMaxAttempts(true) // После исчерпания попыток бросать исключение
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig defaultRetryConfig) {
        return RetryRegistry.of(defaultRetryConfig);
    }
}