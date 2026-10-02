package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    


}
