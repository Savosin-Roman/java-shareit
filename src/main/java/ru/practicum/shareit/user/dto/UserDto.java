package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;
import ru.practicum.shareit.validation.ValidationKeys;

@Data
public class UserDto {

    private Integer id;

    @NotBlank(groups = {ValidationGroups.Create.class}, message = ValidationKeys.USER_EMAIL_NOT_BLANK)
    @Email(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = ValidationKeys.USER_EMAIL_INVALID)
    private String email;

    private String name;
}