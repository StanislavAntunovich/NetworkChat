package ru.geekbrains.chat;

public class AuthException extends Exception {
    public AuthException(String reason) {
        super(reason);
    }
}
