package ru.geekbrains.chat.server;

import ru.geekbrains.chat.AuthException;
import ru.geekbrains.chat.server.persistance.UserRepoReflectionImpl;
import ru.geekbrains.chat.server.persistance.UserRepository;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static ru.geekbrains.chat.MessagesPatterns.*;

public class Server {
    private static final int PULL_SIZE = 1000;
    static final Logger logger = Logger.getLogger(Server.class.getName());

    private boolean isOnline;
    private AuthService authService;
    private ExecutorService executorService;

    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());

    public Server(Connection connection, Class userClass) {
        UserRepository<Integer, User> repository = new UserRepoReflectionImpl(connection, userClass);
        this.authService = new AuthJDBCServiceImpl(repository);

        executorService = Executors.newFixedThreadPool(
                PULL_SIZE, runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });
    }

    //TODO: refactor
    public void sendAddressedMessage(String from, String to, String message) {
        logger.info(String.format("Клиент %s отправил %s сообщение \"%s\"", from, to, message));
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
        logger.info(String.format("Клиент %s подключился", login));

        executorService.execute(clientHandler::startHandling);
        userCameOnline(login);
        clientHandlers.put(login, clientHandler);
    }

    public void unSubscribe(String login) {
        logger.info(String.format("Клиент %s отключился", login));

        clientHandlers.remove(login);
        userCameOffline(login);
    }

    public void startServer(int port) {
        isOnline = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен");
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
                    logger.warning("Ошибка аутентификации: " + e.getMessage());
                    out.writeUTF("/error " + e.getMessage());
                    out.flush();
                }
            }

        } catch (IOException e) {
            logger.severe("Ошибка сервера: " + e.getMessage());
        }
    }

    //TODO перемудрил - разгрести
    private User handshake(String message) throws AuthException {
        final String[] preparedMsg = message.split(" ");
        logger.info("команда от клиента: " + preparedMsg[0]);
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
