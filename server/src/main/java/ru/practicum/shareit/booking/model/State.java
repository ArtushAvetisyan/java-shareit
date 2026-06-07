package ru.practicum.shareit.booking.model;

import java.util.Optional;

public enum State {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<State> fromString(String state) {
        for (State i : values()) {
            if (i.name().equalsIgnoreCase(state)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
}