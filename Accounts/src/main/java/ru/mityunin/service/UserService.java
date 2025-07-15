package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.mapper.UserMapper;
import ru.mityunin.model.User;
import ru.mityunin.repository.UserRepository;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
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
    public void deleteUser(String login) {
        User user = findByLogin(login);
        if (user != null) {
            userRepository.delete(user);
        }
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
        User user = userRepository.findByLogin(userDto.getLogin()).get();
        if (user != null) {
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());
            user.setBirthDate(userDto.getBirthDate());
            userRepository.save(user);
        }
    }
}
