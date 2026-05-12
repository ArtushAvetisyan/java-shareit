package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    void testBookItemRequestDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);
        BookItemRequestDto requestDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookItemRequestDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.toString());
    }

    @Test
    void testBookItemRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\"itemId\":1, \"start\":\"2026-12-10T12:00:00\", \"end\":\"2026-12-11T13:00:00\"}";

        BookItemRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isEqualTo(1);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2026, 12, 10, 12, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2026, 12, 11, 13, 0));
    }
}