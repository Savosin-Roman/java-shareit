package ru.practicum.shareit.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Repository<K, V> {
    Optional<V> getById(K id);

    V save(V entity);

    V update(V entity);

    void delete(K id);
}