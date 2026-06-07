package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approveBooking(long userId, long bookingId, boolean approved);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getUserBookings(long userId, String state, int from, int size);

    List<BookingResponseDto> getOwnerBookings(long userId, String state, int from, int size);
}