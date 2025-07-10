package ru.mityunin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users", schema = "accounts")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password; // будет хранить зашифрованный пароль

    private String firstName;
    private String lastName;

    private String email;

    private LocalDate birthDate;
}
