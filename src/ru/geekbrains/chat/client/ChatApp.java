package ru.geekbrains.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("resources/fxml/main_window.fxml"));
        primaryStage.setTitle("GeekChat");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(380.0);
        primaryStage.setMinWidth(500.0);
        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
    }
}