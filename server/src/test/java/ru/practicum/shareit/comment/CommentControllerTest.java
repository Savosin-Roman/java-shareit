package ru.practicum.shareit.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CreateCommentRequest;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.validation.Headers.USER_ID;

@WebMvcTest(CommentController.class)
@Import(ErrorHandler.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private CommentDto sampleComment() {
        return CommentDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Ivan")
                .created(LocalDateTime.of(2027, 1, 1, 12, 0, 0))
                .build();
    }

    @Test
    void createComment_returns201() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Отличная дрель!");

        when(commentService.create(eq(1L), eq(1L), any(CreateCommentRequest.class)))
                .thenReturn(sampleComment());

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная дрель!"))
                .andExpect(jsonPath("$.authorName").value("Ivan"));
    }

    @Test
    void createComment_userNotFound_returns404() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("text");

        when(commentService.create(eq(99L), eq(1L), any(CreateCommentRequest.class)))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_noCompletedBooking_returns400() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("text");

        when(commentService.create(eq(1L), eq(1L), any(CreateCommentRequest.class)))
                .thenThrow(new ValidationException("Only tenant can comment"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}