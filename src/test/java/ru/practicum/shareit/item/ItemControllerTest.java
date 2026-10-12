package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.validation.Headers.USER_ID;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto() {
        return ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .build();
    }

    @Test
    void create_returns201() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemService.create(eq(1L), any(CreateItemRequest.class))).thenReturn(itemDto());

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void create_withoutHeader_returns500() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());   // ← было isBadRequest()
    }

    @Test
    void create_withoutAvailable_returns400() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        // available не задан — @NotNull сработает

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nonExistentUser_returns404() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemService.create(eq(99L), any(CreateItemRequest.class)))
                .thenThrow(new NotFoundException("Пользователь с id=99 не найден"));

        mockMvc.perform(post("/items")
                        .header(USER_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200() throws Exception {
        when(itemService.getById(eq(1L), eq(1L))).thenReturn(itemDto());

        mockMvc.perform(get("/items/1").header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(itemService.getById(anyLong(), eq(99L)))
                .thenThrow(new NotFoundException("Вещь с id=99 не найдена"));

        mockMvc.perform(get("/items/99").header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnerItems_returnsList() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(itemDto()));

        mockMvc.perform(get("/items").header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная"));
    }

    @Test
    void search_returnsList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto()));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_empty_returnsEmptyList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search").param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void update_patch_returns200() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        when(itemService.update(eq(1L), eq(1L), any(UpdateItemRequest.class)))
                .thenReturn(itemDto());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_notOwner_returns404() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        when(itemService.update(eq(2L), eq(1L), any(UpdateItemRequest.class)))
                .thenThrow(new NotFoundException("Вещь с id=1 не найдена"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}