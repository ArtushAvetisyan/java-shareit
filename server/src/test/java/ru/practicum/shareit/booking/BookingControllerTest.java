package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_shouldCreateBooking() throws Exception {
        long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto inputDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();

        UserDto bookerDto = UserDto.builder().id(userId).name("Pavel").email("user@yandex.ru").build();
        ItemResponseDto item = ItemResponseDto.builder().id(1L).name("Item").description("Item Description").available(true).build();

        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(1L)
                .start(inputDto.getStart())
                .end(inputDto.getEnd())
                .item(item)
                .booker(bookerDto)
                .status(Status.WAITING)
                .build();

        when(bookingService.createBooking(anyLong(), any(BookingRequestDto.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).createBooking(userId, inputDto);
    }

    @Test
    void approveBooking_shouldApproveBooking() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;
        LocalDateTime now = LocalDateTime.now();

        UserDto bookerDto = UserDto.builder().id(2L).name("Pavel").email("booker@yandex.ru").build();
        ItemResponseDto itemDto = ItemResponseDto.builder().id(1L).name("Item").description("Item Description").available(true).build();

        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(itemDto)
                .booker(bookerDto)
                .status(Status.APPROVED)
                .build();

        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approveBooking(userId, bookingId, approved);
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        LocalDateTime now = LocalDateTime.now();

        UserDto bookerDto = UserDto.builder().id(2L).name("Pavel").email("booker@yandex.ru").build();
        ItemResponseDto itemDto = ItemResponseDto.builder().id(1L).name("Item").description("Item Description").available(true).build();

        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(itemDto)
                .booker(bookerDto)
                .status(Status.APPROVED)
                .build();

        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(outputDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.booker.id").value(bookerDto.getId()));

        verify(bookingService).getBookingById(userId, bookingId);
    }

    @Test
    void getUserBookings_shouldReturnUserBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();

        UserDto bookerDto = UserDto.builder().id(userId).name("Pavel").email("user@yandex.ru").build();
        ItemResponseDto itemDto1 = ItemResponseDto.builder().id(1L).name("Item 1").description("Desc 1").available(true).build();
        ItemResponseDto itemDto2 = ItemResponseDto.builder().id(2L).name("Item 2").description("Desc 2").available(true).build();

        BookingResponseDto booking1 = BookingResponseDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(itemDto1)
                .booker(bookerDto)
                .status(Status.APPROVED)
                .build();
        BookingResponseDto booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(now.plusDays(3))
                .end(now.plusDays(4))
                .item(itemDto2)
                .booker(bookerDto)
                .status(Status.WAITING)
                .build();

        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].id").value(booking2.getId()))
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getUserBookings(userId, state, from, size);
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();

        UserDto bookerDto1 = UserDto.builder().id(2L).name("Booker 1").email("booker1@yandex.ru").build();
        UserDto bookerDto2 = UserDto.builder().id(3L).name("Booker 2").email("booker2@yandex.ru").build();

        ItemResponseDto itemDto1 = ItemResponseDto.builder().id(1L).name("Item 1").description("Desc 1").available(true).build();
        ItemResponseDto itemDto2 = ItemResponseDto.builder().id(2L).name("Item 2").description("Desc 2").available(true).build();

        BookingResponseDto booking1 = BookingResponseDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(itemDto1)
                .booker(bookerDto1)
                .status(Status.APPROVED)
                .build();
        BookingResponseDto booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(now.plusDays(3))
                .end(now.plusDays(4))
                .item(itemDto2)
                .booker(bookerDto2)
                .status(Status.WAITING)
                .build();

        when(bookingService.getOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$[1].id").value(booking2.getId()))
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getOwnerBookings(userId, state, from, size);
    }
}