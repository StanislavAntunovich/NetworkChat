package ru.geekbrains.chat.client.history;

import java.util.List;

public interface ChatHistory<M> {
    void logMessage(M message);
    List<M> getAllMessages();
    List<M> getLastMessages(int messagesCount);
}
