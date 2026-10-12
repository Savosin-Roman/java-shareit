package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, CreateItemRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .owner(owner)
                .requestId(request.getRequestId())
                .build();

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            item.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        ItemDto dto = itemMapper.toDto(item);

        // Комментарии видны всем — даже если пусто, отдаём пустой список
        List<CommentDto> comments = commentMapper.toDtoList(
                commentRepository.findAllByItemId(itemId));
        dto.setComments(comments);

        // Брони видны только владельцу
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            dto.setLastBooking(bookingMapper.toDto(
                    bookingRepository.findLastBooking(itemId, now)));
            dto.setNextBooking(bookingMapper.toDto(
                    bookingRepository.findNextBooking(itemId, now)));
        }

        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);

                    dto.setComments(commentMapper.toDtoList(
                            commentRepository.findAllByItemId(item.getId())));

                    dto.setLastBooking(bookingMapper.toDto(
                            bookingRepository.findLastBooking(item.getId(), now)));
                    dto.setNextBooking(bookingMapper.toDto(
                            bookingRepository.findNextBooking(item.getId(), now)));

                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemMapper.toDtoList(itemRepository.searchAvailableByText(text));
    }
}