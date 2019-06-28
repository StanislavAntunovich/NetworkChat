package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.chat.server.User;
import ru.geekbrains.chat.server.persistance.UserRepository;

public class AuthJDBCServiceImpl implements AuthService {
    private UserRepository<User, String> repository;

    public AuthJDBCServiceImpl(UserRepository<User, String> repository) {
        this.repository = repository;
    }

    @Override
    public boolean isAuthorized(User userCandidate) {
        User user = repository.findUserByLogin(userCandidate.getLogin());
        return user != null && user.getPassword().equals(userCandidate.getPassword());
    }

    @Override
    public boolean addNewUser(User userCandidate) {
        if (repository.findUserByLogin(userCandidate.getLogin()) != null) {
            return false;
        }
        repository.addUser(userCandidate);
        return true;
    }
}
