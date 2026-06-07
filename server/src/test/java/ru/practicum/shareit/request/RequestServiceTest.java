package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private User otherUser;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User(1L, "Requester Name", "requester@yandex.ru");
        otherUser = new User(2L, "Other User", "other@yandex.ru");
        itemRequest = new ItemRequest(1L, "Request Description", requester, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto("Item Description");
        item = new Item(1L, "Item Name", "Item Description", true, otherUser, itemRequest);
    }

    @Test
    void getUserRequests_shouldReturnListOfItemRequestResponseDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(requester.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Request Description", result.getFirst().getDescription());
        assertEquals(1, result.getFirst().getItems().size());
        assertEquals("Item Name", result.getFirst().getItems().getFirst().getName());

        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(requester.getId());
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getUserRequests_shouldReturnEmptyListWhenNoRequests() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong())).thenReturn(Collections.emptyList());
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(requester.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(requester.getId());
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getUserRequests_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemRequestService.getUserRequests(requester.getId()));

        assertEquals("Пользователь с id " + requester.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository, never()).findAllByRequesterIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_shouldReturnListOfItemRequestResponseDto() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = PageRequest.of(0, 10, sort);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(Pageable.class))).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(otherUser.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Request Description", result.getFirst().getDescription());
        assertEquals(1, result.getFirst().getItems().size());
        assertEquals("Item Name", result.getFirst().getItems().getFirst().getName());

        verify(userRepository).findById(otherUser.getId());
        verify(itemRequestRepository).findAllByRequesterIdNot(eq(otherUser.getId()), any(Pageable.class));
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getAllRequests_shouldReturnEmptyListWhenNoRequests() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = PageRequest.of(0, 10, sort);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(Pageable.class))).thenReturn(Collections.emptyList());
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(otherUser.getId(), 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(otherUser.getId());
        verify(itemRequestRepository).findAllByRequesterIdNot(eq(otherUser.getId()), any(Pageable.class));
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getAllRequests_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(otherUser.getId(), 0, 10));

        assertEquals("Пользователь с id " + otherUser.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(otherUser.getId());
        verify(itemRequestRepository, never()).findAllByRequesterIdNot(anyLong(), any());
    }

    @Test
    void getRequestById_shouldReturnItemRequestResponseDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), itemRequest.getId());

        assertNotNull(result);
        assertEquals("Request Description", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Item Name", result.getItems().getFirst().getName());

        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).findById(itemRequest.getId());
        verify(itemRepository).findAllByRequestId(requester.getId());
    }

    @Test
    void getRequestById_shouldReturnItemRequestResponseDtoWithNoItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(Collections.emptyList());

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertTrue(result.getItems().isEmpty());

        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).findById(itemRequest.getId());
        verify(itemRepository).findAllByRequestId(itemRequest.getId());
    }

    @Test
    void getRequestById_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(requester.getId(), itemRequest.getId()));

        assertEquals("Пользователь с id " + requester.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void getRequestById_shouldThrowNotFoundExceptionWhenRequestNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(requester.getId(), itemRequest.getId()));

        assertEquals("Запрос с id " + itemRequest.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).findById(itemRequest.getId());
        verify(itemRepository, never()).findAllByRequestId(anyLong());
    }

    @Test
    void createRequest_shouldReturnItemRequestResponseDtoWhenSuccessful() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto result = itemRequestService.createRequest(requester.getId(), itemRequestDto);

        assertNotNull(result);
        assertEquals("Request Description", result.getDescription());
        assertTrue(result.getItems().isEmpty());

        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(requester.getId(), itemRequestDto));

        assertEquals("Пользователь с id " + requester.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(requester.getId());
        verify(itemRequestRepository, never()).save(any());
    }
}