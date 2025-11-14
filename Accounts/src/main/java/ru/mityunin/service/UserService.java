package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.UserDto;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    public ApiResponse<List<UserDto>> findAllExceptLogin(String login) {
        List<UserDto> userList = userRepository
                .findAll()
                .stream()
                .filter(user -> !user.getLogin().equals(login))
                .map(user -> userMapper.userToUserDto(user))
                .toList();
        return ApiResponse.success("List of users, except " + login, userList);
    }

    public ApiResponse<List<PaymentAccountDto>> paymentAccountsByLogin(String login) {
        User user = userRepository.findByLogin(login).get();
        List<PaymentAccountDto> paymentAccountDtoList = user.getPaymentAccounts()
                .stream()
                .map(userMapper::paymentAccountToPaymentAccountDto)
                .toList();

        return ApiResponse.success("List of payment accounts fpr " + login, paymentAccountDtoList);
    }


    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        log.info("Comparing passwords for {}", user.getLogin());
        log.info("Raw password: {}", rawPassword);
        log.info("DB password: {}", user.getPassword());

        boolean result = passwordEncoder.matches(rawPassword, user.getPassword());
        log.info("Match result: {}", result);

        return result;
    }

    @Transactional
    public ApiResponse<Void> deleteUser(String login) {
        User user = findByLogin(login);
        for (PaymentAccount paymentAccount: user.getPaymentAccounts()) {
            if (paymentAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                return ApiResponse.error("Счет " + paymentAccount.getAccountNumber() + " имеет ненулевой баланс. Операция отклонена");
            }
        }
        if (user != null) {
            userRepository.delete(user);
        }
        return ApiResponse.success("Успех удаления аккаунта");
    }

    @Transactional
    public void updateUserPassword(AuthRequest authRequest) {
        User user = userRepository.findByLogin(authRequest.getLogin()).get();
        if (user != null) {
            user.setPassword(authRequest.getPassword());
            saveUser(user);
        }
    }

    @Transactional
    public void updateUserInfo(UserDto userDto) {
        User user = userRepository.findByLogin(userDto.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found with login: " + userDto.getLogin()));

        // Обновляем основные данные пользователя
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setBirthDate(userDto.getBirthDate());

        // Обрабатываем счета
        if (userDto.getPaymentAccounts() != null) {
            // Создаем мап для быстрого поиска счетов по номеру
            Map<String, PaymentAccount> existingAccountsMap = user.getPaymentAccounts().stream()
                    .collect(Collectors.toMap(PaymentAccount::getAccountNumber, account -> account));

            // Очищаем текущий список, но не удаляем счета из БД (orphanRemoval=false)
            user.getPaymentAccounts().clear();

            // Обрабатываем каждый счет из DTO
            for (PaymentAccountDto dto : userDto.getPaymentAccounts()) {
                PaymentAccount account;

                // Если счет с таким номером уже существует - обновляем его
                if (existingAccountsMap.containsKey(dto.getAccountNumber())) {
                    account = existingAccountsMap.get(dto.getAccountNumber());
                    // Обновляем поля, кроме accountNumber и user
                    account.setCurrency(dto.getCurrency());
                    account.setBalance(dto.getBalance());
                    account.setIsDeleted(dto.getIsDeleted() != null ? dto.getIsDeleted() : false);
                } else {
                    // Создаем новый счет
                    account = userMapper.paymentAccountDtoToPaymentAccount(dto);
                    account.setUser(user);
                }

                user.getPaymentAccounts().add(account);
            }
        }

        userRepository.save(user);
    }
}
