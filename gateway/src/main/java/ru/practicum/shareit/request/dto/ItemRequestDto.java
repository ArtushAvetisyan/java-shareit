package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRequestDto {

    @NotBlank(message = "Описание не может быть пустым")
    private String description;
}