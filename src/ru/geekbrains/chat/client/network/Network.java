package ru.geekbrains.chat.client.network;

import javafx.application.Platform;
import ru.geekbrains.chat.AuthException;
import ru.geekbrains.chat.client.Message;
import ru.geekbrains.chat.client.history.ChatHistory;
import ru.geekbrains.chat.client.history.ChatHistoryImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import static ru.geekbrains.chat.MessagesPatterns.*;
import static ru.geekbrains.chat.client.Controller.MESSAGES_TO_SHOW_COUNT;

public class Network {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread receiverThread;

    private IncomeMessageHandler incomeMessageHandler;

    private final String host;

    private final int port;
    private String login;

    private ChatHistory<Message> history;


    public Network(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        this.socket = new Socket(host, port);

        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());


        receiverThread = new Thread(() -> {
            while (true) {
                try {
                    String msg = in.readUTF();
                    if (msg.startsWith("/"))
                        handleServerCommand(msg);
                    else {
                        String[] splitedMsg = msg.split(">", 2);
                        if (!splitedMsg[0].equals(login)) {
                            Message message = new Message(splitedMsg[0], splitedMsg[1]);
                            history.logMessage(message);
                            incomeMessageHandler.handleMessage(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

    }

    private void handleServerCommand(String msg) {
        String[] preparedMsg = msg.split(" ", 2);
        String command = preparedMsg[0];
        //TODO: поменять на switch
        if (preparedMsg[0].equals(USERS_LIST_COMMAND)) {
            if (!preparedMsg[1].isEmpty()) {
                incomeMessageHandler.setOnlineUsersList(Arrays.asList(preparedMsg[1].trim().split(" ")));
            }
            return;
        }
        if (command.equals(USER_CAME_OFLINE_COMMAND)) {
            incomeMessageHandler.removeOnlineUser(preparedMsg[1]);
            return;
        }
        if (command.equals(USER_CAME_ONLINE_COMMAND)) {
            incomeMessageHandler.addOnlineUser(preparedMsg[1]);
            return;
        }
    }

    public void authorize(String login, String password, String action) throws IOException, AuthException {

        String message;
        switch (action) {
            case "Sign Up":
                message = String.format(REGISTRATION_MESSAGE, login, password);
                break;
            case "Log In":
                message = String.format(AUTH_MESSAGE, login, password);
                break;
            default:
                throw new IllegalArgumentException(action);
        }
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String authResponse = in.readUTF();

        if (authResponse.equals("/succeeded")) {
            this.login = login;
            this.history = new ChatHistoryImpl(login);
            for (Message msg : history.getLastMessages(MESSAGES_TO_SHOW_COUNT)) {
                incomeMessageHandler.handleMessage(msg);
            }
            Platform.runLater(() -> receiverThread.start());
            receiverThread.setDaemon(true);
        } else {
            throw new AuthException(authResponse.split(" ", 2)[1]);
        }
    }


    public void requestOnlineUsersList() throws IOException {
        out.writeUTF(REQUEST_ONLINE_USERS);
    }

    public void sendMessage(String to, String message) {
        try {
            if (!to.equals("all")) {
                sendAddressedMessage(to, message);
            } else {
                out.writeUTF(message);
            }
            Message msg = new Message(login, message);
            history.logMessage(msg);
            incomeMessageHandler.handleMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendAddressedMessage(String to, String message) throws IOException {
        out.writeUTF(String.format(ADDRESSED_MESSAGE_PATTERN, to, message));
    }


    public String getLogin() {
        return login;
    }

    public void setIncomeMessageHandler(IncomeMessageHandler incomeMessageHandler) {
        this.incomeMessageHandler = incomeMessageHandler;
    }
}
