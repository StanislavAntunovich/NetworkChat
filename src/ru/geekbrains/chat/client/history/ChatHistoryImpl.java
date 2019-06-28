package ru.geekbrains.chat.client.history;

import ru.geekbrains.chat.client.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static ru.geekbrains.chat.MessagesPatterns.HISTORY_FILENAME_PATTERN;

public class ChatHistoryImpl implements ChatHistory<Message> {

    private PrintWriter writer;
    private Path path;

    public ChatHistoryImpl(String login) throws IOException {
        this.path = Paths.get(String.format(HISTORY_FILENAME_PATTERN, login));
        this.writer = new PrintWriter(Files.newOutputStream(path, APPEND, CREATE), true);
    }

    @Override
    public synchronized void logMessage(Message message) {
        writer.println(stringFromMessage(message));
    }

    @Override
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();

        if (Files.exists(path)) {
            try {
                messages = Files.readAllLines(path).stream()
                        .map(this::messageFromString)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return messages;
    }

    @Override
    public List<Message> getLastMessages(int messagesCount) {
        List<Message> messages = getAllMessages();
        int size = messages.size();

        if (size < messagesCount) {
            return messages;
        }
        return messages.subList(size - messagesCount, size);
    }

    private Message messageFromString(String message) {
        String[] preparedM = message.split(">", 3);
        return new Message(LocalDateTime.parse(preparedM[0]), preparedM[1], preparedM[2]);
    }

    private String stringFromMessage(Message message) {
        return message.getCreated() + ">" + message.getUser() + ">" + message.getMessageText();
    }

}
