package ru.practicum.shareit.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractRepository<K, V> implements Repository<K, V> {

    protected final JdbcTemplate jdbc;
    protected final RowMapper<V> rowMapper;

    protected AbstractRepository(JdbcTemplate jdbc, RowMapper<V> rowMapper) {
        this.jdbc = jdbc;
        this.rowMapper = rowMapper;
    }

    /**
     * Выполняет запрос и возвращает первую строку как {@code Optional};
     * пустой, если строк нет.
     */
    protected Optional<V> findOne(String query, Object... params) {
        List<V> result = jdbc.query(query, rowMapper, params);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    /**
     * Выполняет проверку существования.
     */
    protected boolean exists(String query, Object... params) {
        Integer count = jdbc.queryForObject(query, Integer.class, params);
        return count != null && count > 0;
    }

    /**
     * Выполняет запрос и возвращает все отображённые строки.
     */
    protected List<V> findAll(String query, Object... params) {
        return jdbc.query(query, rowMapper, params);
    }

    /**
     * Выполняет INSERT с {@code RETURN_GENERATED_KEYS} и возвращает новый id;
     * бросает {@code ApiException(INSERT_FAILED)}, если ключ не сгенерирован.
     */
    protected int insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            return id;
        } else {
            throw new ApiException(ErrorCode.INSERT_FAILED);
        }
    }

    /**
     * Выполняет UPDATE;
     * бросает {@code ApiException(UPDATE_FAILED)}, если затронуто 0 строк.
     */
    protected void executeUpdate(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new ApiException(ErrorCode.UPDATE_FAILED);
        }
    }

    /**
     * Подставляет динамический список плейсхолдеров {@code IN (?,?,…)} в шаблон и выполняет
     * {@code findAll}; возвращает {@code List.of()} для пустого {@code ids}.
     */
    protected List<V> findByIds(String queryTemplate, Collection<K> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = queryTemplate.formatted(placeholders);
        return findAll(query, ids.toArray());
    }

    /**
     * Батч-upsert пар {@code (ownerId, relatedId)} через MERGE-запрос;
     * no-op при пустом {@code relatedIds}.
     */
    protected void saveRelation(Integer ownerId, Collection<Integer> relatedIds, String mergeQuery) {
        if (relatedIds.isEmpty()) {
            return;
        }
        List<Object[]> args = relatedIds.stream()
                .map(rid -> new Object[]{ownerId, rid})
                .toList();
        jdbc.batchUpdate(mergeQuery, args);
    }

    /**
     * Выполняет DELETE;
     * бросает {@code ApiException(DELETE_FAILED)}, если удалено 0 строк.
     */
    protected void deleteById(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        if (rowsDeleted == 0) {
            throw new ApiException(ErrorCode.DELETE_FAILED);
        }
    }
}