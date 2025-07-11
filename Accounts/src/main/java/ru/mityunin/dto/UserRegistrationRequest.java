package ru.mityunin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegistrationRequest {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
}
