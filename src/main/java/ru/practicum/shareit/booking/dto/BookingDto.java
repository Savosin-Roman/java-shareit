package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationGroups;
import ru.practicum.shareit.validation.ValidationKeys;

import java.time.LocalDateTime;

@Data
public class BookingDto {

    @NotNull(groups = ValidationGroups.Update.class,
            message = ValidationKeys.BOOKING_ID_NOT_NULL)
    private Integer id;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.BOOKING_START_NOT_NULL)
    private LocalDateTime start;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.BOOKING_END_NOT_NULL)
    private LocalDateTime end;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.BOOKING_ITEM_NOT_NULL)
    private Item item;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.BOOKING_BOOKER_NOT_NULL)
    private User booker;

    private BookingStatus status;
}