package ru.geekbrains.chat.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class MessageCell extends ListCell<Message> {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private HBox messageCellTop;

    @FXML
    private Label labelTimeStamp;

    @FXML
    private TextArea txtMessage;

    @FXML
    private Label labelUser;

    @FXML
    private VBox messageCell;

    public MessageCell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/message_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(Message message, boolean b) {
        super.updateItem(message, b);

        if (message != null && !b) {
            labelTimeStamp.setText(message.getCreated().format(timeFormatter));
            labelUser.setText(message.getUser());
            txtMessage.setText(message.getMessageText());
            messageCellTop.setStyle(String.format("-fx-background-color: %s",
                    message.getUser().equals("Вы") ? "lightgreen" : "lightgray"));

//            labelTimeStamp.setStyle("-fx-padding: 1 0 1 3");
            setGraphic(messageCell);
        } else {
            setGraphic(null);
        }
    }
}
