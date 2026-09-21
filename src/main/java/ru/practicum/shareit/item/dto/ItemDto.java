package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;
import ru.practicum.shareit.validation.ValidationKeys;

@Data
public class ItemDto {

    // УБРАТЬ @NotNull — id приходит из PathVariable, а не из тела
    private Integer id;

    @NotBlank(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.ITEM_NAME_NOT_BLANK)
    private String name;

    @NotBlank(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.ITEM_DESCRIPTION_NOT_BLANK)
    private String description;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.ITEM_AVAILABLE_NOT_BLANK)
    private Boolean available;

    private Integer owner;

    private Integer request;
}