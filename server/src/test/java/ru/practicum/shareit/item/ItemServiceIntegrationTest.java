package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    private User createOwner(String email) {
        return userRepository.save(User.builder()
                .name("Owner").email(email).build());
    }

    private CreateItemRequest createRequest(String name, boolean available) {
        CreateItemRequest request = new CreateItemRequest();
        request.setName(name);
        request.setDescription("Description");
        request.setAvailable(available);
        return request;
    }

    @Test
    void create_shouldSaveItemInDb() {
        User owner = createOwner("owner@test.com");

        ItemDto result = itemService.create(owner.getId(), createRequest("Дрель", true));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void create_ownerNotFound_throws() {
        assertThatThrownBy(() -> itemService.create(99L, createRequest("Дрель", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldUpdateProvidedFields() {
        User owner = createOwner("owner@test.com");
        Long itemId = itemService.create(owner.getId(), createRequest("Дрель", true)).getId();

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");
        request.setAvailable(false);

        ItemDto result = itemService.update(owner.getId(), itemId, request);

        assertThat(result.getName()).isEqualTo("Новая дрель");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getDescription()).isEqualTo("Description");
    }

    @Test
    void update_itemNotFound_throws() {
        User owner = createOwner("owner@test.com");

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("X");

        assertThatThrownBy(() -> itemService.update(owner.getId(), 99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_notOwner_throws() {
        User owner = createOwner("owner@test.com");
        User other = createOwner("other@test.com");
        Long itemId = itemService.create(owner.getId(), createRequest("Дрель", true)).getId();

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("X");

        assertThatThrownBy(() -> itemService.update(other.getId(), itemId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_returnsItem() {
        User owner = createOwner("owner@test.com");
        Long itemId = itemService.create(owner.getId(), createRequest("Дрель", true)).getId();

        ItemDto result = itemService.getById(owner.getId(), itemId);

        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getComments()).isNotNull();
    }

    @Test
    void getById_notFound_throws() {
        assertThatThrownBy(() -> itemService.getById(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_returnsOnlyOwnItems() {
        User owner = createOwner("owner@test.com");
        User other = createOwner("other@test.com");

        itemService.create(owner.getId(), createRequest("Дрель", true));
        itemService.create(owner.getId(), createRequest("Молоток", true));
        itemService.create(other.getId(), createRequest("Пила", true));

        List<ItemDto> result = itemService.getAllByOwner(owner.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllByOwner_userNotFound_throws() {
        assertThatThrownBy(() -> itemService.getAllByOwner(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void search_returnsMatchingAvailableItems() {
        User owner = createOwner("owner@test.com");
        itemService.create(owner.getId(), createRequest("Дрель аккумуляторная", true));
        itemService.create(owner.getId(), createRequest("Молоток", true));

        List<ItemDto> result = itemService.search("дрель");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Дрель");
    }

    @Test
    void search_emptyText_returnsEmpty() {
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void search_unavailableItemsNotReturned() {
        User owner = createOwner("owner@test.com");
        itemService.create(owner.getId(), createRequest("Дрель", false));

        List<ItemDto> result = itemService.search("дрель");

        assertThat(result).isEmpty();
    }
}