package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    List<ItemResponseDto> getAllUserItems(Long userId);

    ItemResponseDto getItemById(Long itemId);

    List<ItemResponseDto> searchItemByText(String text);

    ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto);
}
