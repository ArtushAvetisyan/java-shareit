package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public List<Item> getAllUserItems(Long userId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(userId)).toList();
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> searchItemByText(String text) {
        String query = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(query)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(query)))
                .toList();
    }

    @Override
    public Item saveItem(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
            items.put(item.getId(), item);
            return item;
        } else {
            items.put(item.getId(), item);
            return item;
        }
    }
}