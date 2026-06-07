package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createItem_whenDataIsValid_shouldCreateItem() throws Exception {
        long userId = 1L;
        ItemRequestDto validItem = ItemRequestDto.builder()
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .build();

        when(itemClient.createItem(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).createItem(eq(userId), any());
    }

    @Test
    void createItem_whenNameIsEmpty_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto invalidItem = ItemRequestDto.builder()
                .name("")
                .description("Item Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any());
    }

    @Test
    void createItem_whenDescriptionIsEmpty_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto invalidItem = ItemRequestDto.builder()
                .name("Item Name")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any());
    }

    @Test
    void createItem_whenAvailableIsNull_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto invalidItem = ItemRequestDto.builder()
                .name("Item Name")
                .description("Item Description")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any());
    }

    @Test
    void updateItem_whenItemIdIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = -1L;
        ItemRequestDto item = ItemRequestDto.builder()
                .name("Drill")
                .description("description")
                .available(true)
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any());
    }

    @Test
    void updateItem_whenAllFieldsAreValid_shouldUpdateItem() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemRequestDto validItem = ItemRequestDto.builder()
                .name("Updated Item Name")
                .description("Updated Item Description")
                .available(false)
                .build();

        when(itemClient.updateItem(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(userId), eq(itemId), any());
    }

    @Test
    void updateItem_whenNameIsEmpty_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemRequestDto invalidItem = ItemRequestDto.builder()
                .name("")
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any());
    }

    @Test
    void updateItem_whenDescriptionIsEmpty_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemRequestDto invalidItem = ItemRequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any());
    }

    @Test
    void getItemById_whenItemIdIsNegative_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = -1L;

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getItemById(userId, itemId);
    }

    @Test
    void getItemById_whenGetRequest_shouldReturnItem() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemRequestDto expectedItem = ItemRequestDto.builder()
                .name("Drill")
                .description("description")
                .available(true)
                .build();

        when(itemClient.getItemById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(expectedItem));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemClient).getItemById(eq(userId), eq(itemId));
    }

    @Test
    void getAllItems_whenGetRequest_shouldReturnItems() throws Exception {
        long userId = 1L;

        when(itemClient.getAllUserItems(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).getAllUserItems(eq(userId));
    }

    @Test
    void searchItems_whenGetRequest_shouldReturnItems() throws Exception {
        long userId = 1L;
        String searchText = "text";

        when(itemClient.searchItemByText(anyString())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, userId)
                        .param("text", searchText)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).searchItemByText(eq(searchText));
    }

    @Test
    void searchItems_whenTextIsEmpty_shouldReturnStatusOkAndEmptyList() throws Exception {
        long userId = 1L;
        String searchText = "";

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, userId)
                        .param("text", searchText)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemClient, never()).searchItemByText(anyString());
    }

    @Test
    void addComment_whenDataIsValid_shouldAddComment() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentRequestDto validComment = CommentRequestDto.builder()
                .text("Test comment")
                .build();

        when(itemClient.createComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validComment))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).createComment(eq(userId), eq(itemId), any());
    }

    @Test
    void addComment_whenTextIsEmpty_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentRequestDto invalidComment = CommentRequestDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidComment))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }
}