package ru.geekbrains.chat.server.persistance;

import ru.geekbrains.chat.server.User;

import java.util.List;

public class UserRepositoryImpl implements UserRepository<User, String> {

    @Override
    public User findUserByLogin(String login) {
        return null;
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }
}
