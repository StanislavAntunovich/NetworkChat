package ru.geekbrains.chat.server;

import ru.geekbrains.chat.AuthException;
import ru.geekbrains.chat.server.persistance.UserRepository;
import ru.geekbrains.chat.server.persistance.UserRepositoryImpl;
import ru.geekbrains.chat.server.service.auth.AuthJDBCServiceImpl;
import ru.geekbrains.chat.server.service.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ru.geekbrains.chat.MessagesPatterns.*;

public class Server {

    public Server(Connection connection) {
        UserRepository<User, String> repository = new UserRepositoryImpl(connection);
        this.authService = new AuthJDBCServiceImpl(repository);
    }

    private boolean isOnline;
    private AuthService authService;

    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());

    //TODO: refactor
    public void sendAddressedMessage(String from, String to, String message) {
        ClientHandler toCli = clientHandlers.getOrDefault(to, null);
        ClientHandler fromCli = clientHandlers.get(from);
        if (toCli != null) {
            toCli.sendMessage(String.format(MESSAGE_PATTERN, from, message));
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
                    User user = handshake(authMessage);
                    out.writeUTF("/succeeded");
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

    //TODO перемудрил - разгрести
    private User handshake(String message) throws AuthException {
        final String[] preparedMsg = message.split(" ");
        if (preparedMsg.length != 3) {
            throw new AuthException("Wrong message");  //TODO объединить
        }

        if (preparedMsg[0].equals("/auth")) {
            return checkAuthentication(preparedMsg);
        } else if (preparedMsg[0].equals("/registration")) {
            return registration(preparedMsg);
        } else {
            throw new AuthException("Wrong message");
        }
    }

    private User registration(String[] message) throws AuthException {
        User user = new User(message[1], message[2]);
        if (!authService.addNewUser(user)) {
            throw new AuthException("Login busy");
        }
        return user;
    }

    private User checkAuthentication(String[] authMessage) throws AuthException {
        User user = new User(authMessage[1], authMessage[2]);
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
