package ru.geekbrains.chat.server.service.auth;

import ru.geekbrains.server.User;

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
    public boolean isAuthorized(User user) {
        String pwd = users.get(user.getLogin());
        return pwd != null && pwd.equals(user.getPassword());
    }
}
