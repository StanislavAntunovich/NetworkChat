package ru.geekbrains.chat.server.persistance;

import java.util.List;

/**
 *
 * @param <T> user class
 * @param <S> user's login type
 */
public interface UserRepository<T, S> {
    T findUserByLogin(S login);
    void addUser(T user);
    List<T> getAllUsers();

}
