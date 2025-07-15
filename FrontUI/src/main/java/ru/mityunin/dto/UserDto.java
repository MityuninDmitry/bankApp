package ru.mityunin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDto {
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private List<PaymentAccountDto> paymentAccounts;
}
