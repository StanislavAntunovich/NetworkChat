package ru.geekbrains.chat.server.persistance;

import java.util.List;

/**
 *
 * @param <K> entity ID type
 * @param <E> entity class
 */
public interface Repository<K, E extends Entity<K>> { // подумать над ограничением типа id
    E findById(K id);
    List<E> findAll();
    void insert(E entity);
    void update(E entity);
    void delete(K id);
}
