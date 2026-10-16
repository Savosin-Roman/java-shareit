package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    private User createUser(String email) {
        return userRepository.save(User.builder().name("User").email(email).build());
    }

    private Long createItem(Long ownerId, boolean available) {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(available);
        ItemDto dto = itemService.create(ownerId, request);
        return dto.getId();
    }

    private CreateBookingRequest futureRequest(Long itemId) {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        return request;
    }

    @Test
    void create_shouldSaveBooking() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);

        BookingDto result = bookingService.create(booker.getId(), futureRequest(itemId));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_ownItem_throws() {
        User owner = createUser("owner@test.com");
        Long itemId = createItem(owner.getId(), true);

        assertThatThrownBy(() -> bookingService.create(owner.getId(), futureRequest(itemId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_unavailableItem_throws() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), false);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), futureRequest(itemId)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_itemNotFound_throws() {
        User booker = createUser("booker@test.com");

        assertThatThrownBy(() -> bookingService.create(booker.getId(), futureRequest(99L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_bookerNotFound_throws() {
        User owner = createUser("owner@test.com");
        Long itemId = createItem(owner.getId(), true);

        assertThatThrownBy(() -> bookingService.create(99L, futureRequest(itemId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_byOwner_setsApproved() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        BookingDto result = bookingService.approve(owner.getId(), bookingId, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_reject_setsRejected() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        BookingDto result = bookingService.approve(owner.getId(), bookingId, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_notOwner_throws() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        assertThatThrownBy(() -> bookingService.approve(booker.getId(), bookingId, true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyProcessed_throws() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), bookingId, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_notFound_throws() {
        User owner = createUser("owner@test.com");

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), 99L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_byBooker_returnsBooking() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        BookingDto result = bookingService.getById(booker.getId(), bookingId);

        assertThat(result.getId()).isEqualTo(bookingId);
    }

    @Test
    void getById_byOwner_returnsBooking() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        BookingDto result = bookingService.getById(owner.getId(), bookingId);

        assertThat(result.getId()).isEqualTo(bookingId);
    }

    @Test
    void getById_byStranger_throws() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        User stranger = createUser("stranger@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), bookingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_notFound_throws() {
        User user = createUser("user@test.com");

        assertThatThrownBy(() -> bookingService.getById(user.getId(), 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker_returnsBookings() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        bookingService.create(booker.getId(), futureRequest(itemId));

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByBooker_userNotFound_throws() {
        assertThatThrownBy(() -> bookingService.getAllByBooker(99L, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker_waitingState_returnsWaitingOnly() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        Long bookingId = bookingService.create(booker.getId(), futureRequest(itemId)).getId();
        bookingService.approve(owner.getId(), bookingId, false);

        List<BookingDto> waiting = bookingService.getAllByBooker(booker.getId(), "WAITING");
        List<BookingDto> rejected = bookingService.getAllByBooker(booker.getId(), "REJECTED");

        assertThat(waiting).isEmpty();
        assertThat(rejected).hasSize(1);
    }

    @Test
    void getAllByOwner_returnsBookings() {
        User owner = createUser("owner@test.com");
        User booker = createUser("booker@test.com");
        Long itemId = createItem(owner.getId(), true);
        bookingService.create(booker.getId(), futureRequest(itemId));

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByOwner_userNotFound_throws() {
        assertThatThrownBy(() -> bookingService.getAllByOwner(99L, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }

    // ================= getAllByBooker: CURRENT, PAST, FUTURE =================

    @Test
    void getAllByBooker_current_returnsApprovedCurrentBookings() {
        User owner = createUser("curOwner@test.com");
        User booker = createUser("curBooker@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().minusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(1));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), "CURRENT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingId);
    }

    @Test
    void getAllByBooker_past_returnsApprovedPastBookings() {
        User owner = createUser("pastOwner@test.com");
        User booker = createUser("pastBooker@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().minusDays(5));
        request.setEnd(LocalDateTime.now().minusDays(4));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), "PAST");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByBooker_future_returnsApprovedFutureBookings() {
        User owner = createUser("futOwner@test.com");
        User booker = createUser("futBooker@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(5));
        request.setEnd(LocalDateTime.now().plusDays(6));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), "FUTURE");

        assertThat(result).hasSize(1);
    }

// ================= getAllByOwner: CURRENT, PAST, FUTURE =================

    @Test
    void getAllByOwner_current_returnsApprovedCurrentBookings() {
        User owner = createUser("curOwner2@test.com");
        User booker = createUser("curBooker2@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().minusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(1));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), "CURRENT");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByOwner_past_returnsApprovedPastBookings() {
        User owner = createUser("pastOwner2@test.com");
        User booker = createUser("pastBooker2@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().minusDays(5));
        request.setEnd(LocalDateTime.now().minusDays(4));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), "PAST");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByOwner_future_returnsApprovedFutureBookings() {
        User owner = createUser("futOwner2@test.com");
        User booker = createUser("futBooker2@test.com");
        Long itemId = createItem(owner.getId(), true);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(5));
        request.setEnd(LocalDateTime.now().plusDays(6));
        Long bookingId = bookingService.create(booker.getId(), request).getId();
        bookingService.approve(owner.getId(), bookingId, true);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), "FUTURE");

        assertThat(result).hasSize(1);
    }

// ================= parseState =================

    @Test
    void getAllByBooker_nullState_treatedAsAll() {
        User owner = createUser("nullOwner@test.com");
        User booker = createUser("nullBooker@test.com");
        Long itemId = createItem(owner.getId(), true);
        bookingService.create(booker.getId(), futureRequest(itemId));

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByBooker_unknownState_throws() {
        User user = createUser("unk@test.com");

        assertThatThrownBy(() -> bookingService.getAllByBooker(user.getId(), "UNKNOWN"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_overlappingApprovedBooking_throws() {
        User owner = createUser("overlapOwner@test.com");
        User booker1 = createUser("overlapBooker1@test.com");
        User booker2 = createUser("overlapBooker2@test.com");
        Long itemId = createItem(owner.getId(), true);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        CreateBookingRequest req1 = new CreateBookingRequest();
        req1.setItemId(itemId);
        req1.setStart(start);
        req1.setEnd(end);
        Long bookingId1 = bookingService.create(booker1.getId(), req1).getId();
        bookingService.approve(owner.getId(), bookingId1, true);

        CreateBookingRequest req2 = new CreateBookingRequest();
        req2.setItemId(itemId);
        req2.setStart(start.plusHours(1));
        req2.setEnd(end.plusHours(1));
        Long bookingId2 = bookingService.create(booker2.getId(), req2).getId();

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), bookingId2, true))
                .isInstanceOf(ValidationException.class);
    }
}