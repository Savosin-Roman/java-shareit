package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .name("Test")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .email("test@mail.com")
                .name("Test")
                .build();
    }

    @Test
    void getById_whenExists_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getById(1L);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_whenNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDtoList(List.of(user))).thenReturn(List.of(userDto));

        List<UserDto> result = userService.getAll();

        assertThat(result).containsExactly(userDto);
    }

    @Test
    void create_whenEmailUnique_saves() {
        CreateUserRequest request = new CreateUserRequest("Test", "test@mail.com");

        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.create(request);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_whenEmailExists_throwsConflict() {
        CreateUserRequest request = new CreateUserRequest("Test", "test@mail.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenEmailChangedToExisting_throwsConflict() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("other@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("other@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenNameOnly_updatesName() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return UserDto.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .build();
        });

        UserDto result = userService.update(1L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void delete_whenExists_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_throws() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}