package ru.mityunin.controller;

import org.springframework.web.bind.annotation.*;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.User;
import ru.mityunin.service.UserService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/accounts")
public class UserController {
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
            return ResponseEntity.badRequest().build(); // или более информативный ответ
        }

        User user = userMapper.registrationRequestToUser(request);
        User savedUser = userService.saveUser(user);

        return ResponseEntity.ok(userMapper.userToUserDto(savedUser));
    }
}