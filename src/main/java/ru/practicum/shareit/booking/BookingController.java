package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Забронировать на определённую дату
// Подтвердить бронирование владельцем вещи
// Отклонить бронирование владельцем вещи
// Отменить бронирование создателем брони

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {


}
