package ru.practicum.shareit.item;

public final class ItemQueries {

    private ItemQueries() {
    }

    public static final String FIND_BY_ID = "SELECT * FROM items WHERE id = ?";

    public static final String INSERT = """
            INSERT INTO items (name, description, available, owner)
            VALUES (?, ?, ?, ?)
            """;

    public static final String UPDATE = """
            UPDATE items
            SET name = ?, description = ?, available = ?
            WHERE id = ?
            """;

    public static final String FIND_ALL_OWNER_ITEMS = """
            SELECT *
            FROM items
            WHERE owner = ?
            """;

    public static final String FIND_ITEMS_BY_NAME = """
            SELECT *
            FROM items
            WHERE available = TRUE
              AND (LOWER(name) LIKE LOWER(?)
                   OR LOWER(description) LIKE LOWER(?))
            """;

    public static final String DELETE_ITEMS = "DELETE FROM items WHERE id = ?";
}