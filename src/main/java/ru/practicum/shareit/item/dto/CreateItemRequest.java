package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotBlank(message = "Название обязательно")
    private String name;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotNull(message = "Доступность обязательна")
    private Boolean available;

    private Long requestId;
}