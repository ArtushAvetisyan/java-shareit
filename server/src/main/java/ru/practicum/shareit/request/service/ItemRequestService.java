package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestResponseDto> getUserRequests(long userId);

    List<ItemRequestResponseDto> getAllRequests(long userId, int from, int size);

    ItemRequestResponseDto getRequestById(long userId, long requestId);

    ItemRequestResponseDto createRequest(long userId, ItemRequestDto itemRequestDto);
}