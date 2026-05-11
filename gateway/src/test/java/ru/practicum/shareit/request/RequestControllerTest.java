package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
public class RequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequest_whenDataIsValid_shouldCreateRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto validRequestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestClient.createRequest(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validRequestDto))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestClient).createRequest(eq(userId), any());
    }

    @Test
    void createRequest_whenDescriptionIsBlank_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto invalidRequestDto = ItemRequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidRequestDto))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createRequest(anyLong(), any());
    }

    @Test
    void getUserRequests_whenCalled_shouldReturnRequests() throws Exception {
        long userId = 1L;

        when(itemRequestClient.getUserRequests(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestClient).getUserRequests(eq(userId));
    }

    @Test
    void getAllRequests_whenAllParamsAreValid_shouldReturnRequests() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;

        when(itemRequestClient.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllRequests(eq(userId), eq(from), eq(size));
    }

    @Test
    void getAllRequests_whenFromIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllRequests_whenSizeIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = -1;

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllRequests_whenSizeIsZero_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 0;

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getRequestById_whenRequestIdIsValid_shouldReturnRequest() throws Exception {
        long userId = 1L;
        long requestId = 1L;
        ItemRequestDto validRequestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestClient.getRequestById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(validRequestDto));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestClient).getRequestById(eq(userId), eq(requestId));
    }

    @Test
    void getRequestById_whenRequestIdIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long requestId = -1L;

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getRequestById(anyLong(), anyLong());
    }

    @Test
    void getRequestById_whenRequestIdIsZero_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long requestId = 0L;

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getRequestById(anyLong(), anyLong());
    }
}