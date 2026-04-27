package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerValidationException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        userService.getUserById(userId);
        return itemRepository.getAllUserItems(userId).stream().map(ItemMapper::toItemResponseDto).toList();
    }

    @Override
    public ItemResponseDto getItemById(Long itemId) {
        Item item = itemRepository.getItemById(itemId).orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        return ItemMapper.toItemResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> searchItemByText(String text) {
        if (text.isBlank()) return Collections.emptyList();
        return itemRepository.searchItemByText(text).stream().map(ItemMapper::toItemResponseDto).toList();
    }

    @Override
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto) {
        UserDto userDto = userService.getUserById(userId);
        User owner = UserMapper.toUser(userDto);
        owner.setId(userDto.getId());

        Item item = ItemMapper.toItem(itemRequestDto);
        item.setOwner(owner);
        return ItemMapper.toItemResponseDto(itemRepository.saveItem(item));
    }

    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto) {
        userService.getUserById(userId);
        Item item = itemRepository.getItemById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id " + itemId + " не найден"));
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
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}