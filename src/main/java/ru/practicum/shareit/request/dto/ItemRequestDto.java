package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationGroups;
import ru.practicum.shareit.validation.ValidationKeys;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {

    @NotNull(groups = ValidationGroups.Update.class,
            message = ValidationKeys.ITEM_REQUEST_ID_NOT_NULL)
    private Integer id;

    @NotBlank(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.ITEM_REQUEST_DESCRIPTION_NOT_BLANK)
    private String description;

    @NotNull(groups = {ValidationGroups.Create.class},
            message = ValidationKeys.ITEM_REQUEST_REQUESTOR_NOT_NULL)
    private User requestor;

    private LocalDateTime created;
}