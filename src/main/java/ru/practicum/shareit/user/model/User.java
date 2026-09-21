package ru.practicum.shareit.user.model;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String email;
    private String name;
}