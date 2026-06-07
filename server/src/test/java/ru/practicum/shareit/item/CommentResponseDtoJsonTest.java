package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentResponseDtoJsonTest {

    @Autowired
    private JacksonTester<CommentResponseDto> json;

    @Test
    public void testCommentResponseDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.now().withNano(0);
        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Test comment")
                .itemId(2L)
                .created(created)
                .authorName("Test Author")
                .build();

        JsonContent<CommentResponseDto> result = json.write(responseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Test comment");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Test Author");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.toString());
    }

    @Test
    public void testCommentResponseDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"text\":\"Test comment\",\"itemId\":2,\"authorName\":\"Test Author\",\"created\":\"2026-12-11T13:00:00\"}";
        CommentResponseDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getItemId()).isEqualTo(2);
        assertThat(result.getAuthorName()).isEqualTo("Test Author");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2026, 12, 11, 13, 0));
    }
}