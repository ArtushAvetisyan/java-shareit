package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingShortDto {

    private Long bookingId;
    private Long bookerId;
}