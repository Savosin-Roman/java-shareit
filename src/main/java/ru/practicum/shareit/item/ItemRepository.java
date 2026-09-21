package ru.practicum.shareit.item;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.AbstractRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository extends AbstractRepository<Integer, Item> {

    public ItemRepository(JdbcTemplate jdbc, RowMapper<Item> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<Item> findItemsByName(String text) {
        String pattern = "%" + text.toLowerCase() + "%";
        return findAll(ItemQueries.FIND_ITEMS_BY_NAME, pattern, pattern);
    }

    public List<Item> findAllOwnerItems(Integer ownerId) {
        return findAll(ItemQueries.FIND_ALL_OWNER_ITEMS, ownerId);
    }

    @Override
    public Optional<Item> getById(Integer id) {
        return findOne(ItemQueries.FIND_BY_ID, id);
    }

    @Override
    public Item save(Item item) {
        int id = insert(ItemQueries.INSERT,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner());
        item.setId(id);
        return item;
    }

    @Override
    public Item update(Item item) {
        executeUpdate(ItemQueries.UPDATE,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getId());
        return item;
    }

    @Override
    public void delete(Integer id) {
        deleteById(ItemQueries.DELETE_ITEMS, id);
    }
}