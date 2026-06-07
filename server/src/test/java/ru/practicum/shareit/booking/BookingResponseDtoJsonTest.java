package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    public void testBookingResponseDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(Status.REJECTED)
                .build();

        JsonContent<BookingResponseDto> result = json.write(responseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("REJECTED");
    }

    @Test
    public void testBookingResponseDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"start\":\"2026-12-11T13:00:00\",\"end\":\"2026-12-12T13:00:00\",\"status\":\"REJECTED\"}";
        BookingResponseDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2026, 12, 11, 13, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2026, 12, 12, 13, 0));
        assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
    }
}