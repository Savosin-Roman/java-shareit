package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserServiceImpl userService;
    private final Mapper<UserDto, User> userMapper;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable int id) {
        User user = userService.getUser(id);
        return userMapper.toDto(user);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        List<User> users = userService.getUsers();
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto createUser(@Validated(ValidationGroups.Create.class) @RequestBody UserDto userDto) {
        userDto.setId(null);
        User user = userMapper.toEntity(userDto);
        User saved = userService.saveUser(user);
        return userMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable int id,
                              @Validated(ValidationGroups.Update.class)
                              @RequestBody UserDto userDto) {
        userDto.setId(id);
        User user = userMapper.toEntity(userDto);
        User updated = userService.updateUser(user);
        return userMapper.toDto(updated);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }
}