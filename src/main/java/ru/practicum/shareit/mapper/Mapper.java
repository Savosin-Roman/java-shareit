package ru.practicum.shareit.mapper;

public interface Mapper<F, T> {

    T toEntity(F f);

    F toDto(T t);

}