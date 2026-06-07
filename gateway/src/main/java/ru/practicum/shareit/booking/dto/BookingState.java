package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.exception.UnsupportedStatusException;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static String checkState(String state) {
        if (state == null || state.isBlank()) {
            return "ALL";
        }

        for (BookingState i : values()) {
            if (i.name().equalsIgnoreCase(state)) {
                return state;
            }
        }
        throw new UnsupportedStatusException("Некорректный статус бронирования: " + state);
    }
}