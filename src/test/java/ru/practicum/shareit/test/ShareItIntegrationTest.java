package ru.practicum.shareit.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareItIntegrationTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullUserFlow() throws Exception {
        CreateUserRequest create = new CreateUserRequest();
        create.setEmail("flow@mail.com");
        create.setName("Flow");

        String body = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@mail.com"));

        UpdateUserRequest patch = new UpdateUserRequest();
        patch.setName("Updated Flow");

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Flow"));

        mockMvc.perform(delete("/users/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullItemFlow() throws Exception {
        CreateUserRequest owner = new CreateUserRequest();
        owner.setEmail("owner-flow@mail.com");
        owner.setName("Owner");

        String ownerBody = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(owner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long ownerId = objectMapper.readTree(ownerBody).get("id").asLong();

        CreateItemRequest create = new CreateItemRequest();
        create.setName("Молоток");
        create.setDescription("Тяжёлый");
        create.setAvailable(true);

        String itemBody = mockMvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        long itemId = objectMapper.readTree(itemBody).get("id").asLong();

        mockMvc.perform(get("/items/" + itemId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Молоток"));

        mockMvc.perform(get("/items").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/items/search").param("text", "молот"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        UpdateItemRequest patch = new UpdateItemRequest();
        patch.setAvailable(false);

        mockMvc.perform(patch("/items/" + itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/items/search").param("text", "молот"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}