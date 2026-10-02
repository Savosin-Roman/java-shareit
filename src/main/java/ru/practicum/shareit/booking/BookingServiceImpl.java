package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.ForbiddenException;
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
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto create(Long userId, CreateBookingRequest request) {
        if (request.getStart() == null || request.getEnd() == null) {
            throw new ValidationException("Даты начала и окончания обязательны");
        }
        if (!request.getEnd().isAfter(request.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
        if (request.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + request.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        Booking booking = Booking.builder()
                .start(request.getStart())
                .end(request.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id=" + bookingId + " не найдена"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец вещи может одобрить бронь");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронь уже обработана, текущий статус: " + booking.getStatus());
        }

        if (approved) {
            boolean overlaps = bookingRepository.existsApprovedOverlap(
                    booking.getItem().getId(),
                    booking.getStart(),
                    booking.getEnd(),
                    booking.getId());

            if (overlaps) {
                throw new ValidationException("Вещь уже занята в этот период другой одобренной бронью");
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id=" + bookingId + " не найдена"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронь с id=" + bookingId + " не найдена");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, String stateParam) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        State state = parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(userId);
            case WAITING -> bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED);
            case CURRENT -> bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now))
                    .toList();
            case PAST -> bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .toList();
            case FUTURE -> bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .toList();
        };

        return bookingMapper.toDtoList(bookings);
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, String stateParam) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        State state = parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerId(userId);
            case WAITING -> bookingRepository
                    .findAllByItemOwnerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findAllByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED);
            case CURRENT -> bookingRepository
                    .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now))
                    .toList();
            case PAST -> bookingRepository
                    .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .toList();
            case FUTURE -> bookingRepository
                    .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED)
                    .stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .toList();
        };

        return bookingMapper.toDtoList(bookings);
    }

    private State parseState(String stateParam) {
        if (stateParam == null || stateParam.isBlank()) {
            return State.ALL;
        }
        try {
            return State.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + stateParam);
        }
    }
}