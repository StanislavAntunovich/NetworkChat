package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.chat.server.User;
import ru.geekbrains.chat.server.persistance.UserRepository;

public class AuthJDBCServiceImpl implements AuthService {
    private UserRepository<Integer, User> repository;

    public AuthJDBCServiceImpl(UserRepository<Integer, User> repository) {
        this.repository = repository;
    }

    @Override
    public boolean isAuthorized(User userCandidate) {
        User user = repository.findByLogin(userCandidate.getLogin());
        return user != null && user.getPassword().equals(userCandidate.getPassword());
    }

    @Override
    public boolean addNewUser(User userCandidate) {
        if (repository.findByLogin(userCandidate.getLogin()) != null) {
            return false;
        }
        repository.insert(userCandidate);
        return true;
    }
}
