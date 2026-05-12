package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerValidationException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
public class ItemServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemRequestDto itemRequestDto;
    private ItemResponseDto itemResponseDto;
    private Comment comment;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@yandex.ru");
        booker = new User(2L, "Booker", "booker@yandex.ru");
        item = new Item(1L, "Item", "Item Description", true, owner, null);
        itemRequestDto = new ItemRequestDto(null, "Item Request", "Item request Description", true, null);
        itemResponseDto = ItemMapper.toItemResponseDto(item);

        comment = new Comment(1L, "Comment Text", item, booker, LocalDateTime.now());
        commentRequestDto = new CommentRequestDto("Comment Text");
        commentResponseDto = CommentMapper.toCommentResponseDto(comment);

        lastBooking = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item, booker, Status.APPROVED);
        nextBooking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, booker, Status.APPROVED);
    }

    @Test
    void getAllUserItems_shouldReturnListOfItemResponseDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(lastBooking, nextBooking));
        when(commentRepository.findAllByItemIn(anyList())).thenReturn(List.of(comment));

        List<ItemResponseDto> result = itemService.getAllUserItems(owner.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item", result.getFirst().getName());
        assertNotNull(result.getFirst().getLastBooking());
        assertNotNull(result.getFirst().getNextBooking());
        assertEquals(1, result.getFirst().getComments().size());

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findAllByOwnerId(owner.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), any(), any());
        verify(commentRepository).findAllByItemIn(anyList());
    }

    @Test
    void getAllUserItems_shouldReturnEmptyListWhenNoItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getAllUserItems(owner.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findAllByOwnerId(owner.getId());
        verify(bookingRepository, never()).findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any(Pageable.class));
        verify(commentRepository, never()).findAllByItemIn(anyList());
    }

    @Test
    void getAllUserItems_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.getAllUserItems(owner.getId()));

        assertEquals("Пользователь с id " + owner.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(itemRepository, never()).findAllByOwnerId(anyLong());
    }

    @Test
    void getItemById_shouldReturnItemResponseDtoForOwner() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(comment));
        when(bookingRepository.findAllByItemIdAndStatus(anyLong(), any())).thenReturn(List.of(lastBooking, nextBooking));

        ItemResponseDto result = itemService.getItemById(owner.getId(), item.getId());

        assertNotNull(result);
        assertEquals("Item", result.getName());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());

        verify(itemRepository).findById(item.getId());
        verify(commentRepository).findAllByItemId(item.getId());
        verify(bookingRepository).findAllByItemIdAndStatus(item.getId(), Status.APPROVED);
    }

    @Test
    void getItemById_shouldReturnItemResponseDtoForNonOwner() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(comment));

        ItemResponseDto result = itemService.getItemById(booker.getId(), item.getId());

        assertNotNull(result);
        assertEquals("Item", result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());

        verify(itemRepository).findById(item.getId());
        verify(commentRepository).findAllByItemId(item.getId());
        verify(bookingRepository, never()).findAllByItemIdAndStatus(item.getId(), Status.APPROVED);
    }

    @Test
    void getItemById_shouldThrowNotFoundExceptionWhenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.getItemById(owner.getId(), item.getId()));

        assertEquals("Предмет с id " + item.getId() + " не найден", exception.getMessage());
        verify(itemRepository).findById(item.getId());
        verify(commentRepository, never()).findAllByItemId(anyLong());
        verify(bookingRepository, never()).findAllByItemIdAndStatus(anyLong(), any(Status.class));
    }

    @Test
    void searchItemByText_shouldReturnListOfItemResponseDto() {
        when(itemRepository.searchItemByText(anyString())).thenReturn(List.of(item));

        List<ItemResponseDto> result = itemService.searchItemByText("text");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item", result.getFirst().getName());

        verify(itemRepository).searchItemByText("text");
    }

    @Test
    void searchItemByText_shouldReturnEmptyListWhenNoItemsFound() {
        when(itemRepository.searchItemByText(anyString())).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.searchItemByText("text");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository).searchItemByText("text");
    }

    @Test
    void createItem_shouldReturnItemResponseDtoWhenSuccessful() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(item);

        ItemResponseDto result = itemService.createItem(owner.getId(), itemRequestDto);

        assertNotNull(result);
        assertEquals(itemResponseDto, result);

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).save(any(Item.class));
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void createItem_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.createItem(owner.getId(), itemRequestDto));

        assertEquals("Пользователь с id " + owner.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItem_shouldSetRequestWhenRequestIdIsPresent() {
        ItemRequest request = ItemRequest.builder().id(1L).description("Description").created(LocalDateTime.now()).requester(booker).build();
        ItemRequestDto dtoWithRequest = new ItemRequestDto(null, "Item", "Desc", true, 1L);
        Item itemWithRequest = new Item(1L, "Item", "Desc", true, owner, request);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenReturn(itemWithRequest);

        ItemResponseDto result = itemService.createItem(owner.getId(), dtoWithRequest);

        assertNotNull(result);
        assertEquals(1L, result.getRequestId());
        verify(itemRequestRepository).findById(request.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_shouldThrowNotFoundExceptionWhenRequestIdNotFound() {
        ItemRequestDto dtoWithWrongRequest = new ItemRequestDto(null, "Item", "Desc", true, 99L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(owner.getId(), dtoWithWrongRequest));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        ItemRequestDto updateDto = new ItemRequestDto(null, "Updated Name", null, null, null);
        Item updatedItem = new Item(1L, "Updated Name", "Item Description", true, owner, null);
        ItemResponseDto updatedResponseDto = ItemMapper.toItemResponseDto(updatedItem);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemResponseDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertNotNull(result);
        assertEquals(updatedResponseDto, result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findById(item.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_shouldUpdateOnlyDescriptionWhenOtherFieldsAreNull() {
        ItemRequestDto updateDto = new ItemRequestDto(null, null, "Updated Description", null, null);
        Item updatedItem = new Item(1L, "Item Name", "Updated Description", true, owner, null);
        ItemResponseDto updatedResponseDto = ItemMapper.toItemResponseDto(updatedItem);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemResponseDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertNotNull(result);
        assertEquals(updatedResponseDto, result);
        assertEquals("Item Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(userRepository).findById(anyLong());
        verify(itemRepository).findById(anyLong());
        verify(itemRepository).save(any());
    }

    @Test
    void updateItem_shouldUpdateOnlyAvailableWhenOtherFieldsAreNull() {
        ItemRequestDto updateDto = new ItemRequestDto(null, null, null, false, null);
        Item updatedItem = new Item(1L, "Item Name", "Item Description", false, owner, null);
        ItemResponseDto updatedResponseDto = ItemMapper.toItemResponseDto(updatedItem);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemResponseDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertNotNull(result);
        assertEquals(updatedResponseDto, result);
        assertEquals("Item Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertFalse(result.getAvailable());

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findById(item.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), item.getId(), itemRequestDto));

        assertEquals("Пользователь с id " + owner.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void updateItem_shouldThrowNotFoundExceptionWhenItemNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), item.getId(), itemRequestDto));

        assertEquals("Предмет с id " + item.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findById(item.getId());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_shouldThrowOwnerValidationExceptionWhenUserIsNotOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        OwnerValidationException exception = assertThrows(OwnerValidationException.class, () -> itemService.updateItem(booker.getId(), item.getId(), itemRequestDto));

        assertEquals("Вещь не пренадлежит пользователю. Редактирование запрещено", exception.getMessage());
        verify(userRepository).findById(anyLong());
        verify(itemRepository).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createComment_shouldReturnCommentResponseDtoWhenSuccessful() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any())).thenReturn(comment);

        CommentResponseDto result = itemService.createComment(booker.getId(), item.getId(), commentRequestDto);

        assertNotNull(result);
        assertEquals(commentResponseDto, result);

        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).existsByBookerIdAndItemIdAndStatusAndEndBefore(eq(booker.getId()), eq(item.getId()), any(Status.class), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.createComment(booker.getId(), item.getId(), commentRequestDto));

        assertEquals("Пользователь с id " + booker.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void createComment_shouldThrowNotFoundExceptionWhenItemNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.createComment(booker.getId(), item.getId(), commentRequestDto));

        assertEquals("Предмет с id " + item.getId() + " не найден", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class));
    }

    @Test
    void createComment_shouldThrowValidationExceptionWhenBookingNotFoundOrNotFinished() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.createComment(booker.getId(), item.getId(), commentRequestDto));

        assertEquals("Вы не можете оставить отзыв: аренда не найдена или еще не завершена.", exception.getMessage());
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).existsByBookerIdAndItemIdAndStatusAndEndBefore(eq(booker.getId()), eq(item.getId()), any(Status.class), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any());
    }
}