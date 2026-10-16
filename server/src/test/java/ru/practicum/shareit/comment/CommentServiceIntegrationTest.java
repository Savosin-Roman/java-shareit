package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CreateCommentRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingService bookingService;

    private User createUser(String email) {
        return userRepository.save(User.builder().name("User").email(email).build());
    }

    private Long createItem(Long ownerId) {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);
        return itemService.create(ownerId, request).getId();
    }

    @Test
    void create_shouldSaveComment() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId());

        CreateBookingRequest booking = new CreateBookingRequest();
        booking.setItemId(itemId);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Long bookingId = bookingService.create(booker.getId(), booking).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        CommentDto result = commentService.create(booker.getId(), itemId,
                new CreateCommentRequest("Отличная дрель!"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getAuthorName()).isEqualTo("User");
    }

    @Test
    void create_noCompletedBooking_throws() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId());

        assertThatThrownBy(() -> commentService.create(booker.getId(), itemId,
                new CreateCommentRequest("text")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_userNotFound_throws() {
        User owner = createUser("owner@test.com");
        Long itemId = createItem(owner.getId());

        assertThatThrownBy(() -> commentService.create(99L, itemId,
                new CreateCommentRequest("text")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_itemNotFound_throws() {
        User user = createUser("user@test.com");

        assertThatThrownBy(() -> commentService.create(user.getId(), 99L,
                new CreateCommentRequest("text")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByItemId_returnsComments() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId());

        CreateBookingRequest booking = new CreateBookingRequest();
        booking.setItemId(itemId);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Long bookingId = bookingService.create(booker.getId(), booking).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        commentService.create(booker.getId(), itemId, new CreateCommentRequest("Отлично"));

        assertThat(commentService.getAllByItemId(itemId)).hasSize(1);
    }
}