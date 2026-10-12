package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static ru.practicum.shareit.validation.Headers.USER_ID;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader(USER_ID) Long userId,
            @Valid @RequestBody CreateRequest request) {
        return itemRequestService.create(userId, request);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequestor(
            @RequestHeader(USER_ID) Long userId) {
        return itemRequestService.getAllByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(
            @RequestHeader(USER_ID) Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
}