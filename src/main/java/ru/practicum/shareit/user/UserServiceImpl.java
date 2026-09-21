package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUser(int id) {
        return userRepository.getById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, id));
    }

    @Override
    public List<User> getUsers() {
        return userRepository.getAll();
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User saved = userRepository.save(user);
        log.info("Создан пользователь: id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User updateUser(User user) {

        User existingUser = getUser(user.getId());
        if (user.getEmail() != null
                && !user.getEmail().isBlank()
                && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        }

        User updated = userRepository.update(existingUser);
        log.info("Обновлён пользователь: id={}", updated.getId());
        return updated;
    }

    @Override
    public void deleteUser(int id) {
        User user = getUser(id);
        userRepository.delete(user.getId());
    }
}