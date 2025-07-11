package ru.mityunin.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mityunin.dto.UserDto;
import ru.mityunin.dto.UserRegistrationRequest;
import ru.mityunin.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);

    // Для регистрации
    User registrationRequestToUser(UserRegistrationRequest request);
}
