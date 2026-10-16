package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

    @NotNull
    private Long itemId;

    @NotNull
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    @AssertTrue(message = "Дата начала не может быть в прошлом")
    public boolean isStartNotInPast() {
        if (start == null) {
            return true;
        }
        return start.isAfter(LocalDateTime.now().minusSeconds(2));
    }

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    public boolean isEndAfterStart() {
        if (start == null || end == null) {
            return true;
        }
        return end.isAfter(start);
    }
}