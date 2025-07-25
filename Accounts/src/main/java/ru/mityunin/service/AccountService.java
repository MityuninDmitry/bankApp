package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.model.CashOperation;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.repository.PaymentAccountRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public AccountService(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    private final PaymentAccountRepository paymentAccountRepository;

    public boolean deletePaymentAccount(String accountNumber) {
        log.info("Request for set deletion {}", accountNumber);
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByAccountNumber(accountNumber);
        if (paymentAccounts.isEmpty()) {
            return false;
        }

        for(PaymentAccount account: paymentAccounts) {
            if(account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                return false;
            }
            account.setIsDeleted(true);
            paymentAccountRepository.save(account);
        }
        return true;
    }

    public boolean addPaymentAccount(String accountNumber) {
        log.info("Request for set available {}", accountNumber);
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByAccountNumber(accountNumber);
        if (paymentAccounts.isEmpty()) {
            return false;
        }

        for(PaymentAccount account: paymentAccounts) {
            account.setIsDeleted(false);
            paymentAccountRepository.save(account);
        }
        return true;
    }

    public List<PaymentAccount> createDefaultAccounts(User user) {
        List<PaymentAccount> paymentAccounts = new ArrayList<>();

        PaymentAccount account = new PaymentAccount();
        account.setCurrency("RUB");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setIsDeleted(true);

        paymentAccounts.add(account);

        account = new PaymentAccount();
        account.setCurrency("CNY");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setIsDeleted(true);

        paymentAccounts.add(account);

        account = new PaymentAccount();
        account.setCurrency("USD");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setIsDeleted(true);

        paymentAccounts.add(account);

        return paymentAccounts;
    }

    @Transactional
    public ApiResponse<Void> processOperation(CashOperationRequest cashOperationRequest) {
        String accountNumber = cashOperationRequest.getAccountNumber();
        PaymentAccount paymentAccount = paymentAccountRepository.findByAccountNumber(accountNumber).getFirst();
        if (cashOperationRequest.getAction().equals(CashOperation.DEPOSIT)) {
            paymentAccount.setBalance(paymentAccount.getBalance().add(cashOperationRequest.getMoney()));
            paymentAccountRepository.save(paymentAccount);

        } else if (cashOperationRequest.getAction().equals(CashOperation.WITHDRAWN)) {
            if (paymentAccount.getBalance().compareTo(cashOperationRequest.getMoney()) < 0) {
                return ApiResponse.error("NOT ENOUGHT MONEY");
            } else {
                paymentAccount.setBalance(
                        paymentAccount.getBalance().subtract(cashOperationRequest.getMoney()));
                paymentAccountRepository.save(paymentAccount);
            }
        }
        return ApiResponse.success("OPERATION SUCCESS");
    }
}
