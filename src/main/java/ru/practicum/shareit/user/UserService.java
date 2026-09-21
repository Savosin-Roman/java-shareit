package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    User getUser(int id);

    List<User> getUsers();

    User saveUser(User user);

    User updateUser(User user);

    void deleteUser(int id);
}