package ru.practicum.shareit.booking;

public enum State {
    ALL,        // все
    CURRENT,    // текущие (start <= now <= end, APPROVED)
    PAST,       // завершённые (end < now, APPROVED)
    FUTURE,     // будущие (start > now, APPROVED или WAITING)
    WAITING,    // ожидающие (WAITING)
    REJECTED    // отклонённые (REJECTED)
}
