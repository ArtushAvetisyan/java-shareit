package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    private User booker;
    private User anotherBooker;
    private User owner;
    private User anotherOwner;
    private Item item;
    private Item anotherItem;

    @BeforeEach
    void setUp() {
        booker = userRepository.save(User.builder().name("Pavel").email("pavel@yandex.ru").build());
        anotherBooker = userRepository.save(User.builder().name("Ivan").email("ivan@yandex.ru").build());
        owner = userRepository.save(User.builder().name("Vasiliy").email("vasiliy@yandex.ru").build());
        anotherOwner = userRepository.save(User.builder().name("Katya").email("katya@yandex.ru").build());

        item = itemRepository.save(Item.builder()
                .name("drill")
                .description("heavy")
                .available(true)
                .owner(owner)
                .build());

        anotherItem = itemRepository.save(Item.builder()
                .name("vacuum cleaner")
                .description("for construction")
                .available(true)
                .owner(anotherOwner)
                .build());

        for (int i = 0; i < 5; i++) {
            bookingRepository.save(new Booking(
                    null,
                    LocalDateTime.now().plusDays(i + 1).withNano(0),
                    LocalDateTime.now().plusDays(i + 2).withNano(0),
                    item, booker, Status.WAITING));
        }
        for (int i = 0; i < 7; i++) {
            bookingRepository.save(new Booking(
                    null,
                    LocalDateTime.now().plusDays(i + 1).withNano(0),
                    LocalDateTime.now().plusDays(i + 2).withNano(0),
                    anotherItem, anotherBooker, Status.WAITING));
        }
    }

    @Test
    void getUserBookings_shouldReturnWithCorrectPagination() {
        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "ALL", 2, 2);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getStart()).isBefore(LocalDateTime.now().plusDays(4));
    }

    @Test
    void getUserBookings_shouldReturnUserBookings() {
        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(5);
    }

    @Test
    void getOwnerBookings_shouldReturnAllOwnerBookings() {
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(anotherOwner.getId(), "ALL", 0, 20);

        assertThat(bookings).hasSize(7);
    }
}