package ru.mityunin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.mityunin.service.UserService;
import ru.mityunin.model.User;
import java.time.LocalDate;

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
        user.setFirstName("Dmitry");
        user.setLastName("Mityunin");
        user.setEmail("dmityunin@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        userService.saveUser(user);
    }
}
