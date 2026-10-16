package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toDto_shouldMapAllFields() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@mail.com").build();

        UserDto dto = userMapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void toDto_shouldReturnNullForNull() {
        assertThat(userMapper.toDto(null)).isNull();
    }

    @Test
    void toDtoList_shouldMapList() {
        List<User> users = List.of(
                User.builder().id(1L).name("Ivan").email("ivan@mail.com").build(),
                User.builder().id(2L).name("Petr").email("petr@mail.com").build()
        );

        List<UserDto> dtos = userMapper.toDtoList(users);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getName()).isEqualTo("Petr");
    }
}