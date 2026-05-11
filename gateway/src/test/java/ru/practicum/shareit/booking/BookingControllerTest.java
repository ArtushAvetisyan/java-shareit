package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_whenDataIsValid_shouldCreateBooking() throws Exception {
        long userId = 1L;
        BookItemRequestDto validBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(userId), any());
    }

    @Test
    void createBooking_whenItemIdIsNull_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createBooking_whenStartIsNull_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createBooking_whenEndIsNull_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createBooking_whenStartIsPast_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createBooking_whenEndIsPast_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createBooking_whenEndIsBeforeStart_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookItemRequestDto invalidBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidBooking))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void approveBooking_whenApprovedIsTrue_shouldApproveBooking() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;

        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(eq(userId), eq(bookingId), eq(approved));
    }

    @Test
    void approveBooking_whenApprovedIsFalse_shouldRejectBooking() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = false;

        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(eq(userId), eq(bookingId), eq(approved));
    }

    @Test
    void approveBooking_whenBookingIdIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long bookingId = -1L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).approveBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getBookingById_whenBookingIdIsValid_shouldReturnBooking() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto validBooking = BookItemRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(validBooking));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists());

        verify(bookingClient).getBooking(eq(userId), eq(bookingId));
    }

    @Test
    void getBookingById_whenBookingIdIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long bookingId = -1L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBooking(anyLong(), anyLong());
    }

    @Test
    void getUserBookings_whenAllParamsAreValid_shouldReturnBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;

        when(bookingClient.getUserBookings(anyLong(), any(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(eq(userId), eq(BookingState.ALL.toString()), eq(from), eq(size));
    }

    @Test
    void getUserBookings_whenStateIsInvalid_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "INVALID_STATE";
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getUserBookings_whenFromIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getUserBookings_whenSizeIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = -1;

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getUserBookings_whenSizeIsZero_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 0;

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getOwnerBookings_whenAllParamsAreValid_shouldReturnBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;

        when(bookingClient.getOwnerBookings(anyLong(), any(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(eq(userId), eq(BookingState.ALL.toString()), eq(from), eq(size));
    }

    @Test
    void getOwnerBookings_whenStateIsInvalid_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "INVALID_STATE";
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getOwnerBookings_whenFromIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getOwnerBookings_whenSizeIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = -1;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getOwnerBookings_whenSizeIsZero_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 0;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(), anyInt(), anyInt());
    }
}