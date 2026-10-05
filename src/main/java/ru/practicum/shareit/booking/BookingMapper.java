package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.util.List;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemShortDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .booker(UserShortDto.builder()
                        .id(booking.getBooker().getId())
                        .build())
                .status(booking.getStatus())
                .build();
    }

    public List<BookingDto> toDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .toList();
    }
}