package ru.practicum.shareit.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.model.User;

@Component
public class UserMapper implements Mapper<UserDto, User> {

    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        return dto;
    }

    @Override
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return user;
    }
}