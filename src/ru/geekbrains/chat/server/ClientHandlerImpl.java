package ru.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.geekbrains.chat.MessagesPatterns.MESSAGE_PATTERN;
import static ru.geekbrains.chat.MessagesPatterns.REQUEST_ONLINE_USERS;
import static ru.geekbrains.chat.server.Server.logger;

public class ClientHandlerImpl implements ClientHandler {

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private Server server;
    private String login;

    public ClientHandlerImpl(String login, Socket socket, Server server) throws IOException {
        this.login = login;
        this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

    }

    @Override
    public void startHandling() {
            while (true) {
                try {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        handleCommand(message);
                    } else {
                        //TODO убрать от сюда + выделить сообщение в отдельный класс
                        logger.info(String.format("Клиент %s отправил всем сообщение \"%s\"", login, message));

                        server.sendBroadcastMessage(String.format(MESSAGE_PATTERN, login, message));
                    }

                } catch (IOException ex) {
                    server.unSubscribe(login);
                    break;
                }
            }
    }

    @Override
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopHandling() {
        try {
            socket.close();
        } catch (IOException e) {

        }

        server.unSubscribe(login);

    }

    private void handleCommand(String msg) {
        String[] commandMsg = msg.split(" ", 2);
        String command = commandMsg[0];

        if (command.equals(REQUEST_ONLINE_USERS)) {
            requestUsersList();
            return;
        }

        if (command.equals("/w")) {
            handleAddressedMsg(commandMsg[1]);
            return;
        }
    }

    private void handleAddressedMsg(String msg) {
        String[] preparedMsg = msg.split(" ", 2);
        server.sendAddressedMessage(login, preparedMsg[0], preparedMsg[1]);
    }

    private void requestUsersList() {
        server.sendUsersList(login);
    }

}
