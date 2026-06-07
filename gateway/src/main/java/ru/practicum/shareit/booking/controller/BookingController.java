package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(USER_ID_HEADER) long userId,
                                                @Valid @RequestBody BookItemRequestDto bookingRequestDto) {
        return bookingClient.bookItem(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USER_ID_HEADER) long userId,
                                                 @PathVariable @Positive long bookingId,
                                                 @RequestParam boolean approved) {
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(USER_ID_HEADER) long userId,
                                                 @PathVariable @Positive long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader(USER_ID_HEADER) long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        return bookingClient.getUserBookings(userId, BookingState.checkState(state), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(USER_ID_HEADER) long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                   @RequestParam(defaultValue = "10") @Positive int size) {
        return bookingClient.getOwnerBookings(userId, BookingState.checkState(state), from, size);
    }
}