package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        getUserOrThrow(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> allBookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.APPROVED);
        List<Comment> allComments = commentRepository.findAllByItemIn(items);

        Map<Long, List<Booking>> bookingsByItem = allBookings
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        Map<Long, List<Comment>> commentsByItem = allComments
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        return items.stream()
                .map(item -> {
                    ItemResponseDto dto = ItemMapper.toItemResponseDto(item);
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());

                    dto.setLastBooking(findLastBooking(itemBookings, dateTime));
                    dto.setNextBooking(findNextBooking(itemBookings, dateTime));

                    dto.setComments(itemComments.stream().map(CommentMapper::toCommentResponseDto).toList());

                    return dto;
                })
                .toList();
    }

    @Override
    public ItemResponseDto getItemById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        ItemResponseDto responseDto = ItemMapper.toItemResponseDto(item);
        responseDto.setComments(commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentResponseDto).toList());

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime dateTime = LocalDateTime.now();
            bookingRepository
                    .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(itemId, Status.APPROVED, dateTime)
                    .ifPresent(lBooking -> responseDto.setLastBooking(BookingMapper.toBookingShortDto(lBooking)));
            bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, Status.APPROVED, dateTime)
                    .ifPresent(nBooking -> responseDto.setNextBooking(BookingMapper.toBookingShortDto(nBooking)));
        }
        return responseDto;
    }

    @Override
    public List<ItemResponseDto> searchItemByText(String text) {
        return itemRepository.searchItemByText(text).stream().map(ItemMapper::toItemResponseDto).toList();
    }

    @Override
    @Transactional
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto) {
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemRequestDto);
        item.setOwner(owner);
        return ItemMapper.toItemResponseDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public CommentResponseDto createComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore
                (userId, itemId, Status.APPROVED, LocalDateTime.now())) {
            throw new ValidationException("Вы не можете оставить отзыв: аренда не найдена или еще не завершена.");
        }

        Comment comment = CommentMapper.toComment(commentRequestDto, item, user, LocalDateTime.now());
        return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new OwnerValidationException("Вещь не пренадлежит пользователю. Редактирование запрещено");
        }
        if (itemRequestDto.getName() != null && !itemRequestDto.getName().isBlank()) {
            item.setName(itemRequestDto.getName());
        }
        if (itemRequestDto.getDescription() != null && !itemRequestDto.getDescription().isBlank()) {
            item.setDescription(itemRequestDto.getDescription());
        }
        if (itemRequestDto.getAvailable() != null) {
            item.setAvailable(itemRequestDto.getAvailable());
        }
        return ItemMapper.toItemResponseDto(itemRepository.save(item));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private BookingShortDto findLastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
    }

    private BookingShortDto findNextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
    }
}