package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.service.AccountService;
import ru.mityunin.service.UserService;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AccountService accountService;
    private final UserMapper userMapper;

    public UserController(UserService userService, AccountService accountService, UserMapper userMapper) {
        this.userService = userService;
        this.accountService = accountService;
        this.userMapper = userMapper;
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        User user = userService.findByLogin(login);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userMapper.userToUserDto(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserRegistrationRequest request) {
        // Проверяем, не существует ли уже пользователь с таким логином
        if (userService.findByLogin(request.getLogin()) != null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userMapper.registrationRequestToUser(request);
        List<PaymentAccount> paymentAccounts = accountService.createDefaultAccounts(user);
        user.setPaymentAccounts(paymentAccounts);

        User savedUser = userService.saveUser(user);

        return ResponseEntity.ok(userMapper.userToUserDto(savedUser));
    }

    @PostMapping("/auth")
    public ResponseEntity<UserDto> authenticateUser(@RequestBody AuthRequest authRequest) {
        log.info("Auth request for: {}, pass: {}", authRequest.getLogin(), authRequest.getPassword());
        User user = userService.findByLogin(authRequest.getLogin());

        if (user == null || !userService.checkPassword(user, authRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userMapper.userToUserDto(user));
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody String login) {
        log.info("Delete request for: {}", login);
        User user = userService.findByLogin(login);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        userService.deleteUser(login);

        return ResponseEntity.ok("Success user deletion by login");
    }

    @PostMapping("/update/password")
    public  ResponseEntity<String> updateUserPassword(@RequestBody AuthRequest authRequest) {
        log.info("Update User Password request for: {}", authRequest.getLogin());
        userService.updateUserPassword(authRequest);
        return ResponseEntity.ok("Password updated");
    }

    @PostMapping("/update/userInfo")
    public  ResponseEntity<String> updateUserInfo(@RequestBody UserDto userDto) {

        userService.updateUserInfo(userDto);
        return ResponseEntity.ok("User updated");
    }

    @PostMapping("/delete/paymentAccount")
    public ResponseEntity<Void> deletePaymentAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        if (accountNumber == null || accountNumber.isBlank()) {
            log.warn("Attempt to delete account with empty account number");
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean deleted = accountService.deletePaymentAccount(accountNumber);
            if (deleted) {
                log.info("Account {} successfully deleted", accountNumber);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Account {} not found or already deleted", accountNumber);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting account {}: {}", accountNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add/paymentAccount")
    public ResponseEntity<Void> addPaymentAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        if (accountNumber == null || accountNumber.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean success = accountService.addPaymentAccount(accountNumber);
            if (success) {
                log.info("Account {} successfully available", accountNumber);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Account {} not found or already deleted", accountNumber);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error add account {}: {}", accountNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}