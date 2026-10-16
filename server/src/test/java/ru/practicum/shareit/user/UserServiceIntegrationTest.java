package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    private CreateUserRequest createRequest(String name, String email) {
        return new CreateUserRequest(name, email);
    }

    private Long createUser(String name, String email) {
        return userService.create(createRequest(name, email)).getId();
    }

    @Test
    void create_shouldSaveUserInDb() {
        UserDto result = userService.create(createRequest("Ivan", "ivan@mail.com"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        createUser("Ivan", "ivan@mail.com");

        assertThatThrownBy(() -> userService.create(createRequest("Petr", "ivan@mail.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_shouldChangeName() {
        Long id = createUser("Ivan", "ivan@mail.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Petr");

        UserDto result = userService.update(id, request);

        assertThat(result.getName()).isEqualTo("Petr");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void update_shouldChangeEmail() {
        Long id = createUser("Ivan", "ivan@mail.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@mail.com");

        UserDto result = userService.update(id, request);

        assertThat(result.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void update_emailConflict_throws() {
        createUser("Ivan", "ivan@mail.com");
        Long id2 = createUser("Petr", "petr@mail.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("ivan@mail.com");

        assertThatThrownBy(() -> userService.update(id2, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_userNotFound_throws() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Petr");

        assertThatThrownBy(() -> userService.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_returnsUser() {
        Long id = createUser("Ivan", "ivan@mail.com");

        UserDto result = userService.getById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void getById_notFound_throws() {
        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_returnsAllUsers() {
        createUser("Ivan", "ivan@mail.com");
        createUser("Petr", "petr@mail.com");

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void delete_shouldRemoveUser() {
        Long id = createUser("Ivan", "ivan@mail.com");

        userService.delete(id);

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_notFound_throws() {
        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}