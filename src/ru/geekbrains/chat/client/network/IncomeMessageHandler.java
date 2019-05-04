package ru.geekbrains.chat.client.network;

import java.util.List;

public interface IncomeMessageHandler {
    void handleMessage(String from, String message);

    void addOnlineUser(String userName);

    void removeOnlineUser(String userName);

    void setOnlineUsersList(List<String> users);

}
