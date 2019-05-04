package ru.geekbrains.chat.server;

import ru.geekbrains.chat.AuthException;
import ru.geekbrains.chat.server.service.auth.AuthService;
import ru.geekbrains.chat.server.service.auth.AuthServiceImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ru.geekbrains.chat.MessagesPatterns.*;

public class Server {

    public static void main(String[] args) {
        new Server().startServer(7777);
    }


    private boolean isOnline;
    private AuthService authService = new AuthServiceImpl();

    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());

    //TODO: refactor
    public void sendAddressedMessage(String from, String to, String message) {
        ClientHandler toCli = clientHandlers.getOrDefault(to, null);
        ClientHandler fromCli = clientHandlers.get(from);
        if (toCli != null) {
            toCli.sendMessage(String.format(MESSAGE_PATTERN, from, message));
            fromCli.sendMessage(String.format(MESSAGE_PATTERN, from, message));
        } else {
            fromCli.sendMessage(String.format(USER_OFFLINE_PATTERN, to));
        }
    }

    public void sendBroadcastMessage(String message) {
        for (ClientHandler cl : clientHandlers.values())
            cl.sendMessage(message);
    }

    public void subscribe(String login, ClientHandler clientHandler) {
        userCameOnline(login);
        clientHandlers.put(login, clientHandler);
    }

    public void unSubscribe(String login) {
        clientHandlers.remove(login);
        userCameOffline(login);
    }

    public void startServer(int port) {
        isOnline = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isOnline) {
                Socket candidateSocket = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(candidateSocket.getOutputStream());
                DataInputStream in = new DataInputStream(candidateSocket.getInputStream());

                String authMessage = in.readUTF();
                try {
                    User user = checkAuthentication(authMessage);
                    out.writeUTF("/auth succeeded");
                    this.subscribe(user.getLogin(), new ClientHandlerImpl(user.getLogin(), candidateSocket, this));

                } catch (AuthException e) {
                    e.printStackTrace();
                    out.writeUTF("/error " + e.getMessage());
                    out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User checkAuthentication(String authMessage) throws AuthException {
        String[] authParts = authMessage.split(" ");
        if (!authParts[0].equals("/auth") || authParts.length != 3) {
            throw new AuthException("Wrong auth message");
        }
        User user = new User(authParts[1], authParts[2]);
        if (!authService.isAuthorized(user)) {
            throw new AuthException("Wrong login or password");
        }
        if (clientHandlers.containsKey(user.getLogin())) {
            throw new AuthException("User already logged in");
        }
        return user;
    }

    public void sendUsersList(String userLogin) {
        ClientHandler cl = clientHandlers.get(userLogin);
        if (!clientHandlers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String login : clientHandlers.keySet()) {
                if (!login.equals(userLogin)) {
                    sb.append(login);
                    sb.append(" ");
                }
            }
            cl.sendMessage(String.format(USERS_LIST_PATTERN, sb.toString().trim()));
        }
    }

    private void userCameOnline(String userLogin) {
        sendBroadcastMessage(String.format(USER_CAME_ONLINE_PATTERN, userLogin));
    }

    private void userCameOffline(String userLogin) {
        sendBroadcastMessage(String.format(USER_CAME_OFLINE_PATTERN, userLogin));
    }

}
