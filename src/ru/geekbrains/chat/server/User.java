package ru.geekbrains.chat.server;

import ru.geekbrains.chat.server.persistance.Entity;
import ru.geekbrains.chat.server.persistance.annotation.*;

@Table(name = "users")
public class User implements Entity<Integer> {

    @PrimaryKey
    @Column
    private Integer id;

    @Login
    @Unique
    @Column
    private String login;

    @Password
    @Column
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
