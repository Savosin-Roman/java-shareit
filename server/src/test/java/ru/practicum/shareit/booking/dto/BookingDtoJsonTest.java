package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeDatesInIsoFormat() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2027, 1, 1, 12, 0, 0))
                .end(LocalDateTime.of(2027, 1, 2, 12, 0, 0))
                .build();

        assertThat(json.write(dto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2027-01-01T12:00:00");
    }

    @Test
    void shouldDeserializeDates() throws Exception {
        String content = "{\"id\":1,\"start\":\"2027-01-01T12:00:00\",\"end\":\"2027-01-02T12:00:00\"}";

        BookingDto result = json.parseObject(content);

        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2027, 1, 1, 12, 0, 0));
    }
}