package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.request.dto.CreateRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    private User createUser(String email) {
        return userRepository.save(User.builder().name("User").email(email).build());
    }

    @Test
    void create_shouldSaveRequest() {
        User user = createUser("user@test.com");

        ItemRequestDto result = itemRequestService.create(user.getId(),
                new CreateRequest("Ищу перфоратор"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Ищу перфоратор");
        assertThat(result.getRequestorId()).isEqualTo(user.getId());
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void create_userNotFound_throws() {
        assertThatThrownBy(() -> itemRequestService.create(99L, new CreateRequest("X")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByRequestor_returnsRequestsOrderedByCreatedDesc() {
        User user = createUser("user@test.com");
        itemRequestService.create(user.getId(), new CreateRequest("Первый"));
        itemRequestService.create(user.getId(), new CreateRequest("Второй"));

        List<ItemRequestDto> result = itemRequestService.getAllByRequestor(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllByRequestor_userNotFound_throws() {
        assertThatThrownBy(() -> itemRequestService.getAllByRequestor(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_returnsOtherUsersRequests() {
        User author = createUser("author@test.com");
        User other = createUser("other@test.com");
        itemRequestService.create(author.getId(), new CreateRequest("Запрос автора"));

        List<ItemRequestDto> result = itemRequestService.getAll(other.getId(), 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAll_doesNotReturnOwnRequests() {
        User user = createUser("user@test.com");
        itemRequestService.create(user.getId(), new CreateRequest("Свой"));

        List<ItemRequestDto> result = itemRequestService.getAll(user.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getAll_userNotFound_throws() {
        assertThatThrownBy(() -> itemRequestService.getAll(99L, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_returnsRequestWithItems() {
        User author = createUser("author@test.com");
        User owner = createUser("owner@test.com");
        Long requestId = itemRequestService.create(author.getId(),
                new CreateRequest("Ищу перфоратор")).getId();

        CreateItemRequest itemReq = new CreateItemRequest();
        itemReq.setName("Перфоратор");
        itemReq.setDescription("Мощный");
        itemReq.setAvailable(true);
        itemReq.setRequestId(requestId);
        itemService.create(owner.getId(), itemReq);

        ItemRequestDto result = itemRequestService.getById(author.getId(), requestId);

        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Перфоратор");
    }

    @Test
    void getById_notFound_throws() {
        User user = createUser("user@test.com");

        assertThatThrownBy(() -> itemRequestService.getById(user.getId(), 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_userNotFound_throws() {
        assertThatThrownBy(() -> itemRequestService.getById(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}