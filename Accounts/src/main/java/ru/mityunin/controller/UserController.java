package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.*;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;
import ru.mityunin.service.AccountService;
import ru.mityunin.service.UserService;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<ApiResponse<UserDto>> getUserByLogin(@PathVariable String login) {
        User user = userService.findByLogin(login);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT FOUND"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("USER FOUND",userMapper.userToUserDto(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestBody UserRegistrationRequest request) {
        // Проверяем, не существует ли уже пользователь с таким логином
        if (userService.findByLogin(request.getLogin()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("SUCH USER EXISTS"));
        }

        User user = userMapper.registrationRequestToUser(request);
        List<PaymentAccount> paymentAccounts = accountService.createDefaultAccounts(user);
        user.setPaymentAccounts(paymentAccounts);

        User savedUser = userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("REGISTERED",userMapper.userToUserDto(savedUser)));
    }

    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<UserDto>> authenticateUser(@RequestBody AuthRequest authRequest) {
        log.info("Auth request for: {}, pass: {}", authRequest.getLogin(), authRequest.getPassword());
        User user = userService.findByLogin(authRequest.getLogin());

        if (user == null || !userService.checkPassword(user, authRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("NO USER WITH LOGIN" + authRequest.getLogin()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("GOOD",userMapper.userToUserDto(user)));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestBody String login) {
        log.info("Delete request for: {}", login);
        User user = userService.findByLogin(login);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("NO SUCH USER"));
        }

        ApiResponse<Void> response = userService.deleteUser(login);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/update/password")
    public  ResponseEntity<ApiResponse<Void>> updateUserPassword(@RequestBody AuthRequest authRequest) {
        log.info("Update User Password request for: {}", authRequest.getLogin());
        userService.updateUserPassword(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Password updated"));
    }

    @PostMapping("/update/userInfo")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(@RequestBody UserDto userDto) {

        userService.updateUserInfo(userDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User updated"));
    }

    @PostMapping("/delete/paymentAccount")
    public ResponseEntity<ApiResponse<Void>> deletePaymentAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        if (accountNumber == null || accountNumber.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Attempt to delete account with empty account number"));
        }

        try {
            boolean deleted = accountService.deletePaymentAccount(accountNumber);
            if (deleted) {
                log.info("Account {} successfully deleted", accountNumber);
                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("SUCCESS DELETING"));
            } else {
                log.warn("Account {} not found or already deleted", accountNumber);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT FOUND OR ALREADY DELETED"));
            }
        } catch (Exception e) {
            log.error("Error deleting account {}: {}", accountNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("SOME INTERNAL ERROR"));
        }
    }

    @PostMapping("/add/paymentAccount")
    public ResponseEntity<ApiResponse<Void>> addPaymentAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        if (accountNumber == null || accountNumber.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("WHERE IS ACCOUNT NUMBER IN YOUR REQUEST?"));
        }

        try {
            boolean success = accountService.addPaymentAccount(accountNumber);
            if (success) {
                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Payment Account added"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Payment Account not found or already deleted"));
            }
        } catch (Exception e) {
            log.error("Error add account {}: {}", accountNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("SOME INTERNAL ERROR"));
        }
    }

    @PostMapping("/processOperation")
    public ResponseEntity<ApiResponse<Void>> processOperation(@RequestBody CashOperationRequest cashOperationRequest) {
        log.info("[Accounts] processOperation {}",cashOperationRequest);
        ApiResponse<Void> apiResponse = accountService.processOperation(cashOperationRequest);
        if (apiResponse.isSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }

    }

    @PostMapping("/accountInfo")
    public ResponseEntity<ApiResponse<PaymentAccountDto>> getAccountInfo(@RequestBody String accountNumber) {
        ApiResponse<PaymentAccountDto> paymentAccountApiResponse = accountService.getAccountInfo(accountNumber);
        if (paymentAccountApiResponse.isSuccess()) {
            return ResponseEntity.ok(paymentAccountApiResponse);
        } else  {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(paymentAccountApiResponse);
        }
    }

    @GetMapping("/findAllExcept/{login}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersExceptLogin(@PathVariable String login) {
        log.info("[Accounts] UserController getUsersExceptLogin {}", login);
        ApiResponse<List<UserDto>> response = userService.findAllExceptLogin(login);
        log.info("[Accounts] UserController response {}", response);
        if(response.isSuccess() && !response.getData().isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT FOUND"));
        }
    }

    @GetMapping("/paymentAccounts/{login}")
    public ResponseEntity<ApiResponse<List<PaymentAccountDto>>> paymentAccountsByLogin(@PathVariable String login) {
        log.info("[Accounts] UserController getUsersExceptLogin {}", login);
        ApiResponse<List<PaymentAccountDto>> response = userService.paymentAccountsByLogin(login);
        log.info("[Accounts] UserController response {}", response);
        if(response.isSuccess() && !response.getData().isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT FOUND"));
        }
    }

}