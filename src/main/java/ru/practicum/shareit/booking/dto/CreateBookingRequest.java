package ru.practicum.shareit.booking.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreate {

    @NotBlank(message = "Дата начала бронирования обязательна")
    private LocalDateTime start;

    @NotBlank(message = "Дата окончания бронирования обязательна")
    private LocalDateTime end;

    @NotBlank(message = "Вещь обязательна")
    private Item item;

    @NotBlank(message = "Пользователь который осуществляет бронирование обязателен")
    private User booker;
}
