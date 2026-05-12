package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    public void testItemRequestResponseDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.now().withNano(0);
        ItemRequestResponseDto requestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Test description")
                .created(created)
                .items(Collections.emptyList())
                .build();

        JsonContent<ItemRequestResponseDto> result = json.write(requestResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test description");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.toString());
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    public void testItemRequestResponseDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"description\":\"Test description\",\"created\":\"2026-12-11T13:00:00\",\"items\":[]}";
        ItemRequestResponseDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getDescription()).isEqualTo("Test description");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2026, 12, 11, 13, 0));
        assertThat(result.getItems()).isEmpty();
    }
}