package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAnswerDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private Long ownerId;
}