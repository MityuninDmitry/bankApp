package ru.mityunin.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.model.PaymentAccount;
import ru.mityunin.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "paymentAccounts", source = "paymentAccounts")
    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);

    User registrationRequestToUser(UserRegistrationRequest request);

    @Mapping(target = "user", ignore = true) // чтобы избежать циклических зависимостей
    PaymentAccount paymentAccountDtoToPaymentAccount(PaymentAccountDto paymentAccountDto);

    PaymentAccountDto paymentAccountToPaymentAccountDto(PaymentAccount paymentAccount);

    List<PaymentAccount> paymentAccountDtosToPaymentAccounts(List<PaymentAccountDto> paymentAccountDtos);

    List<PaymentAccountDto> paymentAccountsToPaymentAccountDtos(List<PaymentAccount> paymentAccounts);
}
