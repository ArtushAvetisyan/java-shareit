package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookItemRequestDto {

    @NotNull(message = "Предмет не может быть пустым")
    private Long itemId;

    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    @NotNull(message = "Дата начала не может быть пустой")
    private LocalDateTime start;

    @Future(message = "Дата окончания не может быть в прошлом")
    @NotNull(message = "Дата окончания не может быть пустой")
    private LocalDateTime end;

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    private boolean isEndAfterStart() {
        if (start == null || end == null) {
            return true;
        }
        return end.isAfter(start);
    }
}