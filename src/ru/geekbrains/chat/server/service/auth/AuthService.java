package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.chat.server.User;

public interface AuthService {
    boolean isAuthorized(User userCandidate);
    boolean addNewUser(User userCandidate);
}
