package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemClient.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_HEADER) long userId,
                                              @PathVariable @Positive long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemByText(@RequestParam String text) {
        if (text.isBlank()) return ResponseEntity.ok(Collections.emptyList());
        return itemClient.searchItemByText(text);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) long userId,
                                             @Valid @RequestBody ItemRequestDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(USER_ID_HEADER) long userId,
                                                @PathVariable @Positive long itemId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemClient.createComment(userId, itemId, commentRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) long userId,
                                             @PathVariable @Positive long itemId,
                                             @Valid @RequestBody ItemRequestDto itemDto) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }
}