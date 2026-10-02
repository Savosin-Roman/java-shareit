package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper requestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, CreateRequest request) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ItemRequest itemRequest = ItemRequest.builder()
                .description(request.getDescription())
                .requestor(requestor)
                .build();

        return requestMapper.toDto(requestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllByRequestor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return enrichWithItems(requestRepository.findAllByRequestorId(userId));
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return enrichWithItems(requestRepository.findAllByRequestorIdNot(userId, pageable));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
        return enrichWithItems(List.of(request)).getFirst();
    }

    private List<ItemRequestDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<ItemShortDto>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(itemMapper::toShortDto, Collectors.toList())
                ));

        return requests.stream()
                .map(r -> {
                    ItemRequestDto dto = requestMapper.toDto(r);
                    dto.setItems(itemsByRequest.getOrDefault(r.getId(), List.of()));
                    return dto;
                })
                .toList();
    }
}