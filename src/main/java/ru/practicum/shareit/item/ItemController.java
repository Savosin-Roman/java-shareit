package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    // просмотр информации о конкретной вещи
    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Integer itemId) {
        return itemMapper.toDto(itemService.getById(itemId));
    }

    // просмотр владельцем всех его вещей (только название и описание)
    @GetMapping
    public List<ItemShortDto> getOwnerItems(@RequestHeader(USER_HEADER) Integer userId) {
        return itemService.findAllOwnerItems(userId).stream()
                .map(p -> new ItemShortDto(p.getName(), p.getDescription()))
                .collect(Collectors.toList());
    }

    // поиск вещи по названию
    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.findItemsByName(text).stream()
                .map(itemMapper::toDto)
                .toList();
    }

    // создание вещи
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) Integer userId,
                          @Validated(ValidationGroups.Create.class)
                          @RequestBody ItemDto itemDto) {
        itemDto.setId(null);
        Item item = itemMapper.toEntity(itemDto);
        Item saved = itemService.save(userId, item);
        return itemMapper.toDto(saved);
    }

    // обновление вещи
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) Integer userId,
                          @PathVariable Integer itemId,
                          @Validated(ValidationGroups.Update.class)
                          @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        Item item = itemMapper.toEntity(itemDto);
        Item updated = itemService.update(userId, itemId, item);
        return itemMapper.toDto(updated);
    }
}