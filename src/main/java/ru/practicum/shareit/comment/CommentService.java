package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CreateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, Long itemId, CreateCommentRequest request);

    List<CommentDto> getAllByItemId(Long itemId);
}
