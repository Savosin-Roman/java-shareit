package ru.practicum.shareit.request;

import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper implements Mapper<ItemRequestDto, ItemRequest> {

    @Override
    public ItemRequest toEntity(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(dto.getId());
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setRequestor(dto.getRequestor());
        itemRequest.setCreated(dto.getCreated());
        return itemRequest;
    }

    @Override
    public ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setRequestor(itemRequest.getRequestor());
        dto.setCreated(itemRequest.getCreated());
        return dto;
    }
}