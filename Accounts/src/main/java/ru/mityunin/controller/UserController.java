package ru.mityunin.controller;

import org.springframework.web.bind.annotation.*;
import ru.mityunin.dto.UserDto;
import ru.mityunin.model.User;
import ru.mityunin.service.UserService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/accounts")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        User user = userService.findByLogin(login);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserDto userDto = new UserDto();
        userDto.setLogin(user.getLogin());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setBirthDate(user.getBirthDate());

        return ResponseEntity.ok(userDto);
    }
}