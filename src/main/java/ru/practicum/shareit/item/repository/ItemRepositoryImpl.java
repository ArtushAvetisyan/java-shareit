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
    private static final long STARTING_ID = 1L;
    private long nextId = STARTING_ID;

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
    public Item createItem(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    // Понимаю, что такая реализация не совсем корректна, так как по сути мы уже обновили поля объекта по ссылке в сервисе,
    // но решил оставить так, чтобы не сломать архитектуру
    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }
}