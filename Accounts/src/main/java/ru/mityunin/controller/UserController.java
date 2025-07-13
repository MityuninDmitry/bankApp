package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.User;
import ru.mityunin.service.UserService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/accounts")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
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
}