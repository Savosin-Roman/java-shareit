package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, CreateItemRequest request);

    ItemDto update(Long userId, Long itemId, UpdateItemRequest request);

    ItemDto getById(Long userId, Long itemId);

    List<ItemDto> getAllByOwner(Long userId);

    List<ItemDto> search(String text);
}