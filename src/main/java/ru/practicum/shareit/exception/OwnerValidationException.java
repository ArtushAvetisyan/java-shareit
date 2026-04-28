package ru.practicum.shareit.exception;

public class OwnerValidationException extends RuntimeException {
    public OwnerValidationException(String message) {
        super(message);
    }
}