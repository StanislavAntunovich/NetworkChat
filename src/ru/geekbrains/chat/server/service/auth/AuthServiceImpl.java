package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.chat.server.User;

import java.util.HashMap;
import java.util.Map;

public class AuthServiceImpl implements AuthService {

    private Map<String, String> users = new HashMap<>();

    public AuthServiceImpl() {
        users.put("Master", "ADDQD");
        users.put("Margarita", "qwerty001");
        users.put("Voland", "qwerty002");
        users.put("Begemot", "qwerty003");
    }

    @Override
    public boolean isAuthorized(User userCandidate) {
        String pwd = users.get(userCandidate.getLogin());
        return pwd != null && pwd.equals(userCandidate.getPassword());
    }

    @Override
    public boolean addNewUser(User userCandidate) {
        if (users.containsKey(userCandidate.getLogin())) {
            return false;
        }
        users.put(userCandidate.getLogin(), userCandidate.getPassword());
        return true;
    }
}
