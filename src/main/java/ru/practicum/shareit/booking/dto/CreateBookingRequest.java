package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "ID вещи обязателен")
    private Long itemId;

    @NotNull(message = "Дата начала обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;
}