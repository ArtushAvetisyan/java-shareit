package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        long userId = 1L;
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        ItemResponseDto outputDto = ItemResponseDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(itemService.createItem(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).createItem(userId, inputDto);
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .name("Item")
                .description("Description")
                .available(false)
                .build();

        ItemResponseDto outputDto = ItemResponseDto.builder()
                .id(itemId)
                .name("Updated Item Name")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemService.updateItem(anyLong(), anyLong(), any(ItemRequestDto.class)))
                .thenReturn(outputDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Item Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemService).updateItem(userId, itemId, inputDto);
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        long userId = 1L;
        long itemId = 1L;

        ItemResponseDto outputDto = ItemResponseDto.builder()
                .id(itemId)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(outputDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).getItemById(userId, itemId);
    }

    @Test
    void getAllItemsByUserId_shouldReturnListOfItems() throws Exception {
        long userId = 1L;

        ItemResponseDto item1 = ItemResponseDto.builder()
                .id(1L)
                .name("Item 1")
                .description("Desc 1")
                .available(true)
                .build();
        ItemResponseDto item2 = ItemResponseDto.builder()
                .id(2L)
                .name("Item 2")
                .description("Desc 2")
                .available(false)
                .build();

        when(itemService.getAllUserItems(anyLong())).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item1.getId()))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[0].description").value("Desc 1"))
                .andExpect(jsonPath("$[1].id").value(item2.getId()))
                .andExpect(jsonPath("$[1].name").value("Item 2"))
                .andExpect(jsonPath("$[1].description").value("Desc 2"));

        verify(itemService).getAllUserItems(userId);
    }

    @Test
    void searchItems_shouldReturnListOfItems() throws Exception {
        long userId = 1L;
        String text = "test";

        ItemResponseDto item1 = ItemResponseDto.builder()
                .id(1L)
                .name("Item 1")
                .description("Desc 1")
                .available(true)
                .build();
        ItemResponseDto item2 = ItemResponseDto.builder()
                .id(2L)
                .name("Item 2")
                .description("Desc 2")
                .available(true)
                .build();

        when(itemService.searchItemByText(anyString())).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item1.getId()))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[0].description").value("Desc 1"))
                .andExpect(jsonPath("$[1].id").value(item2.getId()))
                .andExpect(jsonPath("$[1].name").value("Item 2"))
                .andExpect(jsonPath("$[1].description").value("Desc 2"));

        verify(itemService).searchItemByText(text);
    }
}