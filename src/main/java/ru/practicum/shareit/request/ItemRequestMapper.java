package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request) {
        if (request == null) {
            return null;
        }
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .build();
    }

    public List<ItemRequestDto> toDtoList(List<ItemRequest> requests) {
        return requests.stream()
                .map(this::toDto)
                .toList();
    }
}