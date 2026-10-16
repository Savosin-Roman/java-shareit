package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.validation.Headers.USER_ID;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto sampleBooking() {
        return BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2027, 1, 1, 12, 0, 0))
                .end(LocalDateTime.of(2027, 1, 2, 12, 0, 0))
                .item(ItemShortDto.builder().id(1L).name("Дрель").build())
                .booker(UserShortDto.builder().id(2L).build())
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_returns201() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2027, 1, 1, 12, 0, 0));
        request.setEnd(LocalDateTime.of(2027, 1, 2, 12, 0, 0));

        when(bookingService.create(eq(2L), any(CreateBookingRequest.class)))
                .thenReturn(sampleBooking());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBooking_itemNotFound_returns404() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(99L);
        request.setStart(LocalDateTime.of(2027, 1, 1, 12, 0, 0));
        request.setEnd(LocalDateTime.of(2027, 1, 2, 12, 0, 0));

        when(bookingService.create(eq(2L), any(CreateBookingRequest.class)))
                .thenThrow(new NotFoundException("Item 99 not found"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_itemNotAvailable_returns400() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2027, 1, 1, 12, 0, 0));
        request.setEnd(LocalDateTime.of(2027, 1, 2, 12, 0, 0));

        when(bookingService.create(eq(2L), any(CreateBookingRequest.class)))
                .thenThrow(new ValidationException("Item not available"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_returns200() throws Exception {
        BookingDto approved = sampleBooking();
        approved.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(eq(1L), eq(1L), eq(true)))
                .thenReturn(approved);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBooking_notOwner_returns403() throws Exception {
        when(bookingService.approve(eq(2L), eq(1L), anyBoolean()))
                .thenThrow(new ForbiddenException("Only owner can approve"));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID, 2)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveBooking_alreadyProcessed_returns400() throws Exception {
        when(bookingService.approve(eq(1L), eq(1L), anyBoolean()))
                .thenThrow(new ValidationException("Booking already processed"));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID, 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_returns200() throws Exception {
        when(bookingService.getById(eq(1L), eq(1L))).thenReturn(sampleBooking());

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBookingById_notFound_returns404() throws Exception {
        when(bookingService.getById(eq(1L), eq(99L)))
                .thenThrow(new NotFoundException("Booking 99 not found"));

        mockMvc.perform(get("/bookings/99")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByBooker_returnsList() throws Exception {
        when(bookingService.getAllByBooker(eq(1L), anyString()))
                .thenReturn(List.of(sampleBooking()));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllByBooker_userNotFound_returns404() throws Exception {
        when(bookingService.getAllByBooker(eq(99L), anyString()))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByOwner_returnsList() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), anyString()))
                .thenReturn(List.of(sampleBooking()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllByOwner_userNotFound_returns404() throws Exception {
        when(bookingService.getAllByOwner(eq(99L), anyString()))
                .thenThrow(new NotFoundException("User 99 not found"));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID, 99))
                .andExpect(status().isNotFound());
    }
}