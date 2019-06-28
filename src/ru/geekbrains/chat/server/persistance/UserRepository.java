package ru.geekbrains.chat.server.persistance;

public interface UserRepository<K, E extends Entity<K>> extends Repository<K, E> {
    E findByLogin(String login);
}
