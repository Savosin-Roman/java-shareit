package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserMapper.class, ErrorHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    private User user() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@mail.com");
        user.setName("Test");
        return user;
    }

    @Test
    void createUser_returns201() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setName("Test");

        when(userService.saveUser(any(User.class))).thenReturn(user());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void createUser_withoutEmail_returns400() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Test");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withDuplicateEmail_returns409() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setName("Test");

        when(userService.saveUser(any(User.class)))
                .thenThrow(new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_returns200() throws Exception {
        when(userService.getUser(1)).thenReturn(user());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.getUser(anyInt()))
                .thenThrow(new ApiException(ErrorCode.USER_NOT_FOUND, 99));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_returnsList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(user()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateUser_patch_returns200() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Updated");

        when(userService.updateUser(any(User.class))).thenReturn(user());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_conflict_returns409() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("other@mail.com");

        when(userService.updateUser(any(User.class)))
                .thenThrow(new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new ApiException(ErrorCode.USER_NOT_FOUND, 99))
                .when(userService).deleteUser(99);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}