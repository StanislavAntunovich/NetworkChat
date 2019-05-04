package ru.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.chat.AuthException;
import ru.geekbrains.chat.client.network.IncomeMessageHandler;
import ru.geekbrains.chat.client.network.Network;

import java.io.IOException;
import java.util.List;


public class Controller implements IncomeMessageHandler {

    private Alert errorAlert;
    private Network network;
    private boolean isAuthorized;

    @FXML
    private ListView<Message> listMessages;

    @FXML
    private TextField txtMessageInput;

    @FXML
    private Button bttnSendMessage;

    @FXML
    private VBox panelLogin;

    @FXML
    private VBox panelMessages;

    @FXML
    private Button bttnOk;

    @FXML
    private Button bttnCancel;

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private VBox mainLayout;

    @FXML
    private ListView<String> usersOnLine;

    private ObservableList<String> users = FXCollections.observableArrayList("all");

    private Stage stage;

    private ObservableList<Message> messages = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        errorAlert = new Alert(Alert.AlertType.ERROR);

        usersOnLine.setItems(users);
        usersOnLine.getSelectionModel().select(0);

        bttnSendMessage.setOnAction(this::sendMessage);
        txtMessageInput.setOnAction(this::sendMessage);
        bttnOk.setOnAction(this::okBttnCLicked);
        txtPassword.setOnAction(this::okBttnCLicked);
        bttnCancel.setOnAction(this::cancelBttnClicked);

        setAuthorized(false);

        listMessages.setCellFactory(messageListView -> new MessageCell());
        listMessages.setItems(messages);



    }

    private void sendMessage(ActionEvent actionEvent) {

        String message = txtMessageInput.getText();
        String to = usersOnLine.getSelectionModel().getSelectedItem();
        network.sendMessage(to, message);
        txtMessageInput.clear();
        txtMessageInput.requestFocus();
    }


    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        panelLogin.setVisible(!isAuthorized);
        panelLogin.setManaged(!isAuthorized);
        panelMessages.setVisible(isAuthorized);
        panelMessages.setManaged(isAuthorized);
    }

    public boolean authorize(String login, String password) {
        try {
            //TODO:
            network.authorize(login, password);
        } catch (AuthException e) {
            showError("Authorization ERROR", e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void okBttnCLicked(ActionEvent event) {

        try {
            network = new Network("localhost", 7777);

            network.setIncomeMessageHandler(this);

            //TODO: проверка на пустые значения
            String login = txtLogin.getText();
            String password = txtPassword.getText();

            setAuthorized(authorize(login, password));

            network.requestOnlineUsersList();

            stage = (Stage) mainLayout.getScene().getWindow();
            stage.setTitle(stage.getTitle() + " " + login);

            txtMessageInput.requestFocus();

        } catch (IOException e) {
            showError("Network ERROR", "No network connection");
        }
        txtPassword.clear();

    }

    private void cancelBttnClicked(ActionEvent event) {
        System.exit(1);
    }

    private void showError(String title, String errorMessage) {
        errorAlert.setTitle(title);
        errorAlert.setContentText(errorMessage);
        errorAlert.setHeaderText(null);
        errorAlert.showAndWait();
    }

    @Override
    public void handleMessage(String from, String message) {
        final String messageFrom = from.equals(network.getLogin()) ? "Вы" : from;
        Platform.runLater(() -> {
            messages.add(new Message(messageFrom, message));
            listMessages.scrollTo(messages.size()-1);
        });
    }

    @Override
    public void addOnlineUser(String user) {
        Platform.runLater(() -> users.add(user));
    }

    @Override
    public void removeOnlineUser(String user) {
        Platform.runLater(() -> users.remove(user));
    }

    @Override
    public void setOnlineUsersList(List<String> usersList) {
        if (!usersList.isEmpty())
            Platform.runLater(() -> users.addAll(usersList));
    }
}
