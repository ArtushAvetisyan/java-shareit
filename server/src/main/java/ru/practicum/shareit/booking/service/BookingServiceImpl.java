package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerValidationException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(long userId, BookingRequestDto bookingRequestDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingRequestDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Предмет недоступен для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new OwnerValidationException("Нельзя забронировать свой предмет");
        }

        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setStatus(Status.WAITING);
        boolean isBookingExists = bookingRepository.hasOverlappingBookings(item.getId(), List.of(Status.APPROVED, Status.WAITING),
                booking.getStart(), booking.getEnd());

        if (isBookingExists) {
            throw new NotAvailableException("Данный предмет уже забронирован в указанный период");
        }

        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long userId, long bookingId, boolean approved) {
        userRepository.findById(userId).orElseThrow(() -> new ValidationException("Пользователь с id " + userId + " не найден"));
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь с id " + userId + " не является владельцем предмета");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new NotAvailableException("Статус бронирования уже был изменен");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(long userId, long bookingId) {
        User user = getUserOrThrow(userId);
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(user.getId()) && !booking.getItem().getOwner().getId().equals(user.getId())) {
            throw new NotAvailableException("Данные о бронировании могут получить только владелец предмета или забронировавший пользователь");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(long userId, String state, int from, int size) {
        getUserOrThrow(userId);
        State actualState = State.fromString(state).orElseThrow(() -> new ValidationException("Передан недопустимый статус"));
        LocalDateTime dateTime = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size);

        List<Booking> bookings = switch (actualState) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageable);
            case CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, dateTime, dateTime, pageable);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, dateTime, pageable);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, dateTime, pageable);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toBookingResponseDto).toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(long userId, String state, int from, int size) {
        getUserOrThrow(userId);
        State actualState = State.fromString(state).orElseThrow(() -> new ValidationException("Передан недопустимый статус"));
        LocalDateTime dateTime = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size);

        List<Booking> bookings = switch (actualState) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable);
            case CURRENT ->
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, dateTime, dateTime, pageable);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, dateTime, pageable);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, dateTime, pageable);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toBookingResponseDto).toList();
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
    }

    private Booking getBookingOrThrow(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронь с id " + bookingId + " не найдена"));
    }
}