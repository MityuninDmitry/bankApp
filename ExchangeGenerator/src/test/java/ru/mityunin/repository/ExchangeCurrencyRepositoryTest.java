package ru.mityunin.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.ExchangeCurrency;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExchangeCurrencyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExchangeCurrencyRepository exchangeCurrencyRepository;

    private LocalDateTime now = LocalDateTime.now();
    private LocalDateTime earlier = now.minusHours(1);
    private LocalDateTime later = now.plusHours(1);

    @Test
    void findByCurrencyFromAndCurrencyToOrderByLocalDateTimeAsc_ShouldReturnOrderedResults() {
        // Arrange
        ExchangeCurrency ec1 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, earlier);
        ExchangeCurrency ec2 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 76, now);
        ExchangeCurrency ec3 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 77, later);

        entityManager.persist(ec1);
        entityManager.persist(ec2);
        entityManager.persist(ec3);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> result = exchangeCurrencyRepository
                .findByCurrencyFromAndCurrencyToOrderByLocalDateTimeAsc(CurrencyType.USD, CurrencyType.RUB);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getLocalDateTime()).isEqualTo(earlier);
        assertThat(result.get(1).getLocalDateTime()).isEqualTo(now);
        assertThat(result.get(2).getLocalDateTime()).isEqualTo(later);
    }

    @Test
    void findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc_ShouldReturnOrderedResults() {
        // Arrange
        ExchangeCurrency ec1 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 85, earlier);
        ExchangeCurrency ec2 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 86, now);
        ExchangeCurrency ec3 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 87, later);

        entityManager.persist(ec1);
        entityManager.persist(ec2);
        entityManager.persist(ec3);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> result = exchangeCurrencyRepository
                .findByCurrencyFromAndCurrencyToOrderByLocalDateTimeDesc(CurrencyType.CNY, CurrencyType.RUB);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getLocalDateTime()).isEqualTo(later);
        assertThat(result.get(1).getLocalDateTime()).isEqualTo(now);
        assertThat(result.get(2).getLocalDateTime()).isEqualTo(earlier);
    }

    @Test
    void findByLocalDateTime_ShouldReturnCorrectRecords() {
        // Arrange
        ExchangeCurrency ec1 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, now);
        ExchangeCurrency ec2 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 85, now);
        ExchangeCurrency ec3 = createExchangeCurrency(CurrencyType.USD, CurrencyType.CNY, 90, earlier); // Different time

        entityManager.persist(ec1);
        entityManager.persist(ec2);
        entityManager.persist(ec3);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> result = exchangeCurrencyRepository.findByLocalDateTime(now);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ExchangeCurrency::getCurrencyFrom)
                .containsExactlyInAnyOrder(CurrencyType.USD, CurrencyType.CNY);
        assertThat(result).extracting(ExchangeCurrency::getLocalDateTime)
                .allMatch(time -> time.equals(now));
    }

    @Test
    void findLatestDateTime_ShouldReturnMostRecentDateTime() {
        // Arrange
        ExchangeCurrency ec1 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, earlier);
        ExchangeCurrency ec2 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 85, now);
        ExchangeCurrency ec3 = createExchangeCurrency(CurrencyType.RUB, CurrencyType.CNY, 95, later);

        entityManager.persist(ec1);
        entityManager.persist(ec2);
        entityManager.persist(ec3);
        entityManager.flush();

        // Act
        LocalDateTime result = exchangeCurrencyRepository.findLatestDateTime();

        // Assert
        assertThat(result).isEqualTo(later);
    }

    @Test
    void findLatestDateTime_WhenNoRecords_ShouldReturnNull() {
        // Act
        LocalDateTime result = exchangeCurrencyRepository.findLatestDateTime();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByCurrencyFromAndCurrencyTo_WhenNoMatchingRecords_ShouldReturnEmptyList() {
        // Arrange
        ExchangeCurrency ec = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, now);
        entityManager.persist(ec);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> result = exchangeCurrencyRepository
                .findByCurrencyFromAndCurrencyToOrderByLocalDateTimeAsc(CurrencyType.USD, CurrencyType.CNY);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByLocalDateTime_WhenNoMatchingRecords_ShouldReturnEmptyList() {
        // Arrange
        ExchangeCurrency ec = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, now);
        entityManager.persist(ec);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> result = exchangeCurrencyRepository.findByLocalDateTime(earlier);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveExchangeCurrency() {
        // Arrange
        ExchangeCurrency exchangeCurrency = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, now);

        // Act
        ExchangeCurrency saved = exchangeCurrencyRepository.save(exchangeCurrency);
        ExchangeCurrency found = exchangeCurrencyRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getCurrencyFrom()).isEqualTo(CurrencyType.USD);
        assertThat(found.getCurrencyTo()).isEqualTo(CurrencyType.RUB);
        assertThat(found.getValue()).isEqualTo(75);
        assertThat(found.getLocalDateTime()).isEqualTo(now);
    }

    @Test
    void shouldFindAllCurrencies() {
        // Arrange
        ExchangeCurrency ec1 = createExchangeCurrency(CurrencyType.USD, CurrencyType.RUB, 75, now);
        ExchangeCurrency ec2 = createExchangeCurrency(CurrencyType.CNY, CurrencyType.RUB, 85, now);

        entityManager.persist(ec1);
        entityManager.persist(ec2);
        entityManager.flush();

        // Act
        List<ExchangeCurrency> all = exchangeCurrencyRepository.findAll();

        // Assert
        assertThat(all).hasSize(2);
    }

    private ExchangeCurrency createExchangeCurrency(CurrencyType from, CurrencyType to, int value, LocalDateTime time) {
        ExchangeCurrency ec = new ExchangeCurrency();
        ec.setCurrencyFrom(from);
        ec.setCurrencyTo(to);
        ec.setValue(value);
        ec.setLocalDateTime(time);
        return ec;
    }
}