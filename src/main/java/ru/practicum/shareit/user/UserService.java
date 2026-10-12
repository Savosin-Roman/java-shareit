package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(CreateUserRequest request);

    UserDto update(Long userId, UpdateUserRequest request);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);
}