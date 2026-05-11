package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestResponseDto outputDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).createRequest(userId, inputDto);
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        long userId = 1L;
        long requestId = 1L;

        ItemRequestResponseDto outputDto = ItemRequestResponseDto.builder()
                .id(requestId)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(outputDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).getRequestById(userId, requestId);
    }

    @Test
    void getRequestsByRequester_shouldReturnListOfRequests() throws Exception {
        long userId = 1L;

        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a hammer")
                .created(LocalDateTime.now().minusHours(2))
                .items(List.of())
                .build();
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need a drill")
                .created(LocalDateTime.now().minusHours(1))
                .items(List.of())
                .build();

        when(itemRequestService.getUserRequests(anyLong())).thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].description").value("Need a hammer"))
                .andExpect(jsonPath("$[1].id").value(request2.getId()))
                .andExpect(jsonPath("$[1].description").value("Need a drill"));

        verify(itemRequestService).getUserRequests(userId);
    }

    @Test
    void getAllRequests_shouldReturnListOfRequests() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;

        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a hammer")
                .created(LocalDateTime.now().minusDays(1))
                .items(List.of())
                .build();
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need a drill")
                .created(LocalDateTime.now().minusDays(2))
                .items(List.of())
                .build();

        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].description").value("Need a hammer"))
                .andExpect(jsonPath("$[1].id").value(request2.getId()))
                .andExpect(jsonPath("$[1].description").value("Need a drill"));

        verify(itemRequestService).getAllRequests(userId, from, size);
    }
}