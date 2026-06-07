package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @GetMapping
    public List<ItemResponseDto> getAllUserItems(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(@RequestHeader(USER_ID_HEADER) long userId,
                                       @PathVariable long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItemByText(@RequestParam String text) {
        return itemService.searchItemByText(text);
    }

    @PostMapping
    public ItemResponseDto createItem(@RequestHeader(USER_ID_HEADER) long userId,
                                      @RequestBody ItemRequestDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto createComment(@RequestHeader(USER_ID_HEADER) long userId,
                                            @PathVariable long itemId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.createComment(userId, itemId, commentRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@RequestHeader(USER_ID_HEADER) long userId,
                                      @PathVariable long itemId,
                                      @RequestBody ItemRequestDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }
}