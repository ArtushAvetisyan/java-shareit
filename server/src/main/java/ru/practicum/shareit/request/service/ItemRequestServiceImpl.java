package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.RequestAnswerDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<ItemRequestResponseDto> getUserRequests(long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return fillRequestsWithAnswers(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(long userId, int from, int size) {
        getUserOrThrow(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = PageRequest.of(from / size, size, sort);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(userId, pageable);
        return fillRequestsWithAnswers(requests);
    }

    @Override
    public ItemRequestResponseDto getRequestById(long userId, long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = getRequestOrThrow(requestId);
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<RequestAnswerDto> answerDtoList = items.stream().map(ItemRequestMapper::toRequestAnswerDto).toList();
        return ItemRequestMapper.toItemRequestResponseDto(request, answerDtoList);
    }

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestMapper.toItemRequestResponseDto(itemRequestRepository.save(itemRequest), Collections.emptyList());
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private ItemRequest getRequestOrThrow(long requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }

    private List<ItemRequestResponseDto> fillRequestsWithAnswers(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream().map(itemRequest -> {
                    List<Item> requestItems = itemsByRequestId.getOrDefault(itemRequest.getId(), Collections.emptyList());
                    List<RequestAnswerDto> answerDtoList = requestItems.stream().map(ItemRequestMapper::toRequestAnswerDto).toList();
                    return ItemRequestMapper.toItemRequestResponseDto(itemRequest, answerDtoList);
                })
                .collect(Collectors.toList());
    }
}