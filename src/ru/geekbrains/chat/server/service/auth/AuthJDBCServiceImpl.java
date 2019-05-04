package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.chat.server.User;
import ru.geekbrains.chat.server.persistance.UserRepository;

public class AuthJDBCServiceImpl implements AuthService {
    private UserRepository repository;

    @Override
    public boolean isAuthorized(User user) {
        return false;
    }
}
