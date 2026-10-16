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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

    private ItemDto sampleItem() {
        return ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .comments(List.of())
                .build();
    }

    @Test
    void createItem_returns201() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemService.create(eq(1L), any(CreateItemRequest.class))).thenReturn(sampleItem());

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_userNotFound_returns404() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemService.create(eq(99L), any(CreateItemRequest.class)))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(post("/items")
                        .header(USER_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_returns200() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        when(itemService.update(eq(1L), eq(1L), any(UpdateItemRequest.class)))
                .thenReturn(sampleItem());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateItem_notOwner_returns404() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        when(itemService.update(eq(2L), eq(1L), any(UpdateItemRequest.class)))
                .thenThrow(new NotFoundException("Item 1 not found"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemById_returns200() throws Exception {
        when(itemService.getById(eq(1L), eq(1L))).thenReturn(sampleItem());

        mockMvc.perform(get("/items/1")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItemById_notFound_returns404() throws Exception {
        when(itemService.getById(eq(1L), eq(99L)))
                .thenThrow(new NotFoundException("Item 99 not found"));

        mockMvc.perform(get("/items/99")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnerItems_returnsList() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(sampleItem()));

        mockMvc.perform(get("/items")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_returnsList() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(sampleItem()));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_emptyText_returnsEmptyList() throws Exception {
        when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}