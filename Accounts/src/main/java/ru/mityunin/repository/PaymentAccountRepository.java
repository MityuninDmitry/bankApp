package ru.mityunin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;

import java.util.List;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    List<PaymentAccount> findByUser(User user);
    List<PaymentAccount> findByAccountNumber(String accountNumber);
}
