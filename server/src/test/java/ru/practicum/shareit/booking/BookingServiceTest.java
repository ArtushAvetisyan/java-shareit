package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        booker = new User(1L, "Booker Name", "booker@example.com");
        owner = new User(2L, "Owner Name", "owner@example.com");
        item = new Item(1L, "Item Name", "Item Description", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking = new Booking(1L, start, end, item, booker, Status.WAITING);
        bookingRequestDto = BookingRequestDto.builder().itemId(item.getId()).start(start).end(end).build();
        bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
    }

    @Test
    void createBooking_shouldReturnBookingResponseDtoWhenSuccessful() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.hasOverlappingBookings(anyLong(), anyList(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(booker.getId(), bookingRequestDto);

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);
        assertEquals(Status.WAITING, result.getStatus());

        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).hasOverlappingBookings(eq(item.getId()), anyList(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));

        assertEquals("Пользователь с id " + booker.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldThrowNotFoundExceptionWhenItemNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));

        assertEquals("Предмет с id " + item.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldThrowValidationExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));

        assertEquals("Предмет недоступен для бронирования", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowOwnerValidationExceptionWhenBookerIsOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        OwnerValidationException exception = assertThrows(OwnerValidationException.class, () -> bookingService.createBooking(owner.getId(), bookingRequestDto));

        assertEquals("Нельзя забронировать свой предмет", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldThrowNotAvailableExceptionWhenOverlappingBookingsExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.hasOverlappingBookings(anyLong(), anyList(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);

        NotAvailableException exception = assertThrows(NotAvailableException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));

        assertEquals("Данный предмет уже забронирован в указанный период", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).hasOverlappingBookings(eq(item.getId()), anyList(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_shouldReturnApprovedBookingResponseDtoWhenApproved() {
        booking.setStatus(Status.WAITING);
        Booking approvedBooking = new Booking(booking.getId(), booking.getStart(), booking.getEnd(), booking.getItem(), booking.getBooker(), Status.APPROVED);
        BookingResponseDto approvedBookingResponseDto = BookingMapper.toBookingResponseDto(approvedBooking);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertNotNull(result);
        assertEquals(approvedBookingResponseDto, result);
        assertEquals(Status.APPROVED, result.getStatus());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository).save(any());
    }

    @Test
    void approveBooking_shouldReturnRejectedBookingResponseDtoWhenRejected() {
        booking.setStatus(Status.WAITING);
        Booking rejectedBooking = new Booking(booking.getId(), booking.getStart(), booking.getEnd(), booking.getItem(), booking.getBooker(), Status.REJECTED);
        BookingResponseDto rejectedBookingResponseDto = BookingMapper.toBookingResponseDto(rejectedBooking);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(rejectedBooking);

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertNotNull(result);
        assertEquals(rejectedBookingResponseDto, result);
        assertEquals(Status.REJECTED, result.getStatus());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository).save(any());
    }

    @Test
    void approveBooking_shouldThrowValidationExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));

        assertEquals("Пользователь с id " + owner.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository, never()).findById(anyLong());
    }

    @Test
    void approveBooking_shouldThrowNotFoundExceptionWhenBookingNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));

        assertEquals("Бронь с id " + booking.getId() + " не найдена", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_shouldThrowValidationExceptionWhenUserIsNotOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.approveBooking(booker.getId(), booking.getId(), true));

        assertEquals("Пользователь с id " + booker.getId() + " не является владельцем предмета", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_shouldThrowNotAvailableExceptionWhenStatusAlreadyChanged() {
        booking.setStatus(Status.APPROVED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotAvailableException exception = assertThrows(NotAvailableException.class, () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));

        assertEquals("Статус бронирования уже был изменен", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingById_shouldReturnBookingResponseDtoWhenUserIsBooker() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(booker.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void getBookingById_shouldReturnBookingResponseDtoWhenUserIsOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(owner.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void getBookingById_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booker.getId(), booking.getId()));

        assertEquals("Пользователь с id " + booker.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository, never()).findById(anyLong());
    }

    @Test
    void getBookingById_shouldThrowNotFoundExceptionWhenBookingNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booker.getId(), booking.getId()));

        assertEquals("Бронь с id " + booking.getId() + " не найдена", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void getBookingById_shouldThrowNotAvailableExceptionWhenUserIsNotBookerOrOwner() {
        User stranger = new User(3L, "Stranger", "stranger@yandex.ru");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(stranger));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotAvailableException exception = assertThrows(NotAvailableException.class, () -> bookingService.getBookingById(stranger.getId(), booking.getId()));

        assertEquals("Данные о бронировании могут получить только владелец предмета или забронировавший пользователь", exception.getMessage());
        verify(userRepository).findById(stranger.getId());
        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void getUserBookings_ALL_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class));
    }

    @Test
    void getUserBookings_CURRENT_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "CURRENT", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getUserBookings_PAST_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "PAST", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getUserBookings_FUTURE_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "FUTURE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getUserBookings_WAITING_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "WAITING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), any(Status.class), any(Pageable.class));
    }

    @Test
    void getUserBookings_REJECTED_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "REJECTED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), any(Status.class), any(Pageable.class));
    }

    @Test
    void getUserBookings_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(booker.getId(), "ALL", 0, 10));

        assertEquals("Пользователь с id " + booker.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository, never()).findAllByBookerIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class));
    }

    @Test
    void getUserBookings_shouldThrowValidationExceptionWhenInvalidState() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.getUserBookings(booker.getId(), "INVALID_STATE", 0, 10));

        assertEquals("Передан недопустимый статус", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository, never()).findAllByBookerIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_ALL_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_CURRENT_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "CURRENT", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_PAST_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "PAST", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_FUTURE_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "FUTURE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_WAITING_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "WAITING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), any(Status.class), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_REJECTED_shouldReturnListOfBookingResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "REJECTED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingResponseDto, result.getFirst());

        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), any(Status.class), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(owner.getId(), "ALL", 0, 10));

        assertEquals("Пользователь с id " + owner.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository, never()).findAllByItemOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getOwnerBookings_shouldThrowValidationExceptionWhenInvalidState() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.getOwnerBookings(owner.getId(), "INVALID_STATE", 0, 10));

        assertEquals("Передан недопустимый статус", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository, never()).findAllByItemOwnerIdOrderByStartDesc(anyLong(), any());
    }
}