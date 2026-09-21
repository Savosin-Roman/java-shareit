package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<Item> findItemsByName(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.findItemsByName(text);
    }

    @Override
    public List<Item> findAllOwnerItems(Integer ownerId) {
        userRepository.getById(ownerId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, ownerId));
        return itemRepository.findAllOwnerItems(ownerId);
    }

    @Override
    public Item getById(Integer id) {
        return itemRepository.getById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.ITEM_NOT_FOUND, id));
    }

    @Override
    @Transactional
    public Item save(Integer ownerId, Item item) {
        userRepository.getById(ownerId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, ownerId));

        item.setOwner(ownerId);
        Item saved = itemRepository.save(item);
        log.info("Создана вещь: id={}, name={}, ownerId={}",
                saved.getId(), saved.getName(), saved.getOwner());
        return saved;
    }

    @Override
    @Transactional
    public Item update(Integer userId, Integer itemId, Item item) {
        Item existing = getById(itemId);

        if (!existing.getOwner().equals(userId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, itemId);
        }

        if (item.getName() != null && !item.getName().isBlank()) {
            existing.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            existing.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existing.setAvailable(item.getAvailable());
        }

        Item updated = itemRepository.update(existing);
        log.info("Обновлена вещь: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Override
    public void delete(Integer id) {
        getById(id);
        itemRepository.delete(id);
        log.info("Удалена вещь: id={}", id);
    }
}