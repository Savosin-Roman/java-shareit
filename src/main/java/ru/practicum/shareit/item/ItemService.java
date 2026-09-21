package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<Item> findItemsByName(String text);

    List<Item> findAllOwnerItems(Integer ownerId);

    Item getById(Integer id);

    Item save(Integer ownerId, Item item);

    Item update(Integer userId, Integer itemId, Item item);

    void delete(Integer id);
}