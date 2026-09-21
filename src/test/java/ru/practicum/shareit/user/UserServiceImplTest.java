package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@mail.com");
        user.setName("Test");
    }

    @Test
    void getUser_whenExists_returnsUser() {
        when(userRepository.getById(1)).thenReturn(Optional.of(user));

        User result = userService.getUser(1);

        assertThat(result).isEqualTo(user);
        verify(userRepository).getById(1);
    }

    @Test
    void getUser_whenNotFound_throwsApiException() {
        when(userRepository.getById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getUsers_returnsAll() {
        when(userRepository.getAll()).thenReturn(List.of(user));

        List<User> result = userService.getUsers();

        assertThat(result).containsExactly(user);
    }

    @Test
    void saveUser_whenEmailUnique_saves() {
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.saveUser(user);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    void saveUser_whenEmailExists_throwsConflict() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.saveUser(user))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenEmailChangedToExisting_throwsConflict() {
        User update = new User();
        update.setId(1);
        update.setEmail("other@mail.com");

        when(userRepository.getById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(update))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).update(any());
    }

    @Test
    void updateUser_whenNameOnly_updatesName() {
        User update = new User();
        update.setId(1);
        update.setName("New Name");

        when(userRepository.getById(1)).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(update);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void deleteUser_whenExists_deletes() {
        when(userRepository.getById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository).delete(1);
    }

    @Test
    void deleteUser_whenNotFound_throws() {
        when(userRepository.getById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository, never()).delete(anyInt());
    }
}