package ru.practicum.shareit.comment;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CreateCommentRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long itemId, CreateCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean completed = bookingRepository.existsCompletedBooking(
                userId, itemId, LocalDateTime.now());
        if (!completed) {
            throw new ValidationException("Оставить комментарий может только тот, кто арендовал вещь");
        }

        Comment comment = Comment.builder()
                .text(request.getText())
                .item(item)
                .author(author)
                .build();
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getAllByItemId(Long itemId) {
        return commentMapper.toDtoList(commentRepository.findAllByItemId(itemId));
    }
}
