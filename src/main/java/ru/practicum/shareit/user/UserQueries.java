package ru.practicum.shareit.user;

public final class UserQueries {

    private UserQueries() {
    }

    public static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    public static final String FIND_ALL = "SELECT * FROM users";
    public static final String FIND_ALL_BY_IDS = "SELECT * FROM users WHERE id IN (%s)";
    public static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM users WHERE email = ?";

    public static final String INSERT = """
            INSERT INTO users (email, name)
            VALUES (?, ?)
            """;

    public static final String UPDATE = """
            UPDATE users
            SET email = ?, name = ?
            WHERE id = ?
            """;

    public static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
}