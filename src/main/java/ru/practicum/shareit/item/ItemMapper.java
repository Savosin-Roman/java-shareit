package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public List<ItemDto> toDtoList(List<Item> items) {
        return items.stream()
                .map(this::toDto)
                .toList();
    }
}