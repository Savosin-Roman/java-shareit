package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto() {
        return UserDto.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();
    }

    @Test
    void createUser_returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@mail.com");
        request.setName("Test");

        when(userService.create(any(CreateUserRequest.class))).thenReturn(userDto());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void createUser_withoutEmail_returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withDuplicateEmail_returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@mail.com");
        request.setName("Test");

        when(userService.create(any(CreateUserRequest.class)))
                .thenThrow(new ConflictException("Email уже занят"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_returns200() throws Exception {
        when(userService.getById(1L)).thenReturn(userDto());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=99 не найден"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_returnsList() throws Exception {
        when(userService.getAll()).thenReturn(List.of(userDto()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateUser_patch_returns200() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");

        when(userService.update(eq(1L), any(UpdateUserRequest.class))).thenReturn(userDto());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_conflict_returns409() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("other@mail.com");

        when(userService.update(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new ConflictException("Email уже занят"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("Пользователь с id=99 не найден"))
                .when(userService).delete(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}