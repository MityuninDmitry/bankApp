package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.CashOperation;
import ru.mityunin.model.CurrencyType;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.repository.PaymentAccountRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final PaymentAccountRepository paymentAccountRepository;
    private final UserMapper userMapper;
    public AccountService(PaymentAccountRepository paymentAccountRepository, UserMapper userMapper) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.userMapper = userMapper;
    }



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
        account.setCurrency(CurrencyType.RUB);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setIsDeleted(true);

        paymentAccounts.add(account);

        return paymentAccounts;
    }

    @Transactional
    public ApiResponse<Void> processOperation(CashOperationRequestDto cashOperationRequestDto) {
        String accountNumber = cashOperationRequestDto.getAccountNumber();
        PaymentAccount paymentAccount = paymentAccountRepository.findByAccountNumber(accountNumber).getFirst();

        if (cashOperationRequestDto.getAction().equals(CashOperation.DEPOSIT)) {
            paymentAccount.setBalance(paymentAccount.getBalance().add(cashOperationRequestDto.getMoney()));
            paymentAccountRepository.save(paymentAccount);

        } else if (cashOperationRequestDto.getAction().equals(CashOperation.WITHDRAWN)) {
            log.info("[Accounts] current money value {}, try withdrawn {}", paymentAccount.getBalance(), cashOperationRequestDto.getMoney() );
            if (paymentAccount.getBalance().compareTo(cashOperationRequestDto.getMoney()) < 0) {
                return ApiResponse.error("Не достаточно денег. На балансе: " + paymentAccount.getBalance() + ", списывается: " + cashOperationRequestDto.getMoney());
            } else {
                paymentAccount.setBalance(
                        paymentAccount.getBalance().subtract(cashOperationRequestDto.getMoney()));
                paymentAccountRepository.save(paymentAccount);
            }
        }
        return ApiResponse.success("Успех");
    }

    public ApiResponse<PaymentAccountDto> getAccountInfo(String accountNumber) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByAccountNumber(accountNumber).getFirst();
        if (paymentAccount.getIsDeleted()) {
            return ApiResponse.error("NO ACCOUNT INFO FOR " + accountNumber + ": it's deleted");
        } else  {
            return ApiResponse.success("There is account number info", userMapper.paymentAccountToPaymentAccountDto(paymentAccount));
        }
    }
}
