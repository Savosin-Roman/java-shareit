package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
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
                .comments(List.of())   // пустой список по умолчанию
                .build();
    }

    public List<ItemDto> toDtoList(List<Item> items) {
        return items.stream()
                .map(this::toDto)
                .toList();
    }

    public ItemShortDto toShortDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    public List<ItemShortDto> toShortDtoList(List<Item> items) {
        return items.stream()
                .map(this::toShortDto)
                .toList();
    }
}