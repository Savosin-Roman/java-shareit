package ru.practicum.shareit.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.mapper.AbstractRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends AbstractRepository<Integer, User> {

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<User> getAll() {
        return findAll(UserQueries.FIND_ALL);
    }

    public List<User> findAllByIds(Collection<Integer> ids) {
        return findByIds(UserQueries.FIND_ALL_BY_IDS, ids);
    }

    public boolean existsByEmail(String email) {
        return exists(UserQueries.EXISTS_BY_EMAIL, email);
    }

    @Override
    public Optional<User> getById(Integer id) {
        return findOne(UserQueries.FIND_BY_ID, id);
    }

    @Override
    public User save(User user) {
        int id = insert(UserQueries.INSERT,
                user.getEmail(),
                user.getName());
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        executeUpdate(UserQueries.UPDATE,
                user.getEmail(),
                user.getName(),
                user.getId());
        return user;
    }

    @Override
    public void delete(Integer id) {
        deleteById(UserQueries.DELETE_USER, id);
    }
}