package ru.mityunin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.service.UserService;
import ru.mityunin.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByLogin("testUser") != null) {
            return;
        }
        User user = new User();
        user.setLogin("testUser");
        user.setPassword("password123");
        user.setFirstName("SomeName");
        user.setLastName("SomeLastName");
        user.setEmail("LastName@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        // Создаем счета
        List<PaymentAccount> accounts = List.of(
                createAccount(CurrencyType.RUB, new BigDecimal("2.00"), user)
        );

        user.setPaymentAccounts(accounts);

        userService.saveUser(user);
    }

    private PaymentAccount createAccount(CurrencyType currency, BigDecimal balance, User user) {
        PaymentAccount account = new PaymentAccount();
        account.setCurrency(currency);
        account.setBalance(balance);
        account.setUser(user);
        account.setIsDeleted(false);
        return account;
    }

}


