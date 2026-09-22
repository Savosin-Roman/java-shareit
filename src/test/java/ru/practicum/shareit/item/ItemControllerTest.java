package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.validation.Headers.USER_ID;

@WebMvcTest(ItemController.class)
@Import({ItemMapper.class, ErrorHandler.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private Item item() {
        Item item = new Item();
        item.setId(1);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setOwner(1);
        return item;
    }

    @Test
    void create_returns201() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);

        when(itemService.save(anyInt(), any(Item.class))).thenReturn(item());

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withoutAvailable_returns400() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nonExistentUser_returns404() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);

        when(itemService.save(anyInt(), any(Item.class)))
                .thenThrow(new ApiException(ErrorCode.USER_NOT_FOUND, 99));

        mockMvc.perform(post("/items")
                        .header(USER_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200() throws Exception {
        when(itemService.getById(1)).thenReturn(item());

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(itemService.getById(anyInt()))
                .thenThrow(new ApiException(ErrorCode.ITEM_NOT_FOUND, 99));

        mockMvc.perform(get("/items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnerItems_returnsList() throws Exception {
        when(itemService.findAllOwnerItems(1)).thenReturn(List.of(item()));

        mockMvc.perform(get("/items").header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].description").value("Аккумуляторная"));
    }

    @Test
    void search_returnsList() throws Exception {
        when(itemService.findItemsByName(anyString())).thenReturn(List.of(item()));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_empty_returnsEmptyList() throws Exception {
        when(itemService.findItemsByName(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search").param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void update_patch_returns200() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Новая дрель");

        when(itemService.update(anyInt(), anyInt(), any(Item.class))).thenReturn(item());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Новая дрель");

        when(itemService.update(anyInt(), anyInt(), any(Item.class)))
                .thenThrow(new ApiException(ErrorCode.ACCESS_DENIED, 1));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}