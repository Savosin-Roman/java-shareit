package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.CreateRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.validation.Headers.USER_ID;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto sampleRequest() {
        return ItemRequestDto.builder()
                .id(1L)
                .description("Ищу перфоратор")
                .requestorId(1L)
                .created(LocalDateTime.of(2027, 1, 1, 12, 0, 0))
                .items(List.of())
                .build();
    }

    private ItemRequestDto requestWithItem() {
        return ItemRequestDto.builder()
                .id(1L)
                .description("Ищу перфоратор")
                .requestorId(1L)
                .created(LocalDateTime.of(2027, 1, 1, 12, 0, 0))
                .items(List.of(ItemShortDto.builder().id(2L).name("Перфоратор").build()))
                .build();
    }

    @Test
    void createRequest_returns201() throws Exception {
        CreateRequest request = new CreateRequest("Ищу перфоратор");

        when(itemRequestService.create(eq(1L), any(CreateRequest.class)))
                .thenReturn(sampleRequest());

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Ищу перфоратор"))
                .andExpect(jsonPath("$.requestorId").value(1));
    }

    @Test
    void createRequest_userNotFound_returns404() throws Exception {
        CreateRequest request = new CreateRequest("Ищу перфоратор");

        when(itemRequestService.create(eq(99L), any(CreateRequest.class)))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByRequestor_returnsList() throws Exception {
        when(itemRequestService.getAllByRequestor(1L))
                .thenReturn(List.of(requestWithItem()));

        mockMvc.perform(get("/requests")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Перфоратор"));
    }

    @Test
    void getAllByRequestor_userNotFound_returns404() throws Exception {
        when(itemRequestService.getAllByRequestor(99L))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(get("/requests")
                        .header(USER_ID, 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(itemRequestService.getAll(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(sampleRequest()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_withDefaults_returnsList() throws Exception {
        when(itemRequestService.getAll(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(sampleRequest()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_userNotFound_returns404() throws Exception {
        when(itemRequestService.getAll(eq(99L), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200() throws Exception {
        when(itemRequestService.getById(eq(2L), eq(1L)))
                .thenReturn(requestWithItem());

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Перфоратор"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(itemRequestService.getById(eq(1L), eq(99L)))
                .thenThrow(new NotFoundException("Request 99 not found"));

        mockMvc.perform(get("/requests/99")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }
}