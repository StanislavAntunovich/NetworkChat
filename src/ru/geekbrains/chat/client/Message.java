package ru.geekbrains.chat.client;

import java.time.LocalDateTime;

public class Message {

    private LocalDateTime created;
    private String user;
    private String messageText;

    public Message() {
        created = LocalDateTime.now();
    }

    public Message(String user, String messageText) {
        this.user = user;
        this.messageText = messageText;
        created = LocalDateTime.now();
    }

    public Message(LocalDateTime created, String user, String messageText) {
        this.created = created;
        this.user = user;
        this.messageText = messageText;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

}
