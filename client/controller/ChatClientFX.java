package controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.ChatClientConnection;
import ui.ChatView;
import managerui.*;

public class ChatClientFX extends Application {
    private ChatClientConnection connection;
    private ChatView chatView;
    public static ChatController chatController1;

    @Override
    public void start(Stage primaryStage) throws Exception {
        new Notification().loadSounds();
        
        chatView = new ChatView();
        chatController1 = new ChatController(chatView, primaryStage);
        
        Scene scene = new Scene(chatView, 900, 600);
        primaryStage.setScene(scene);
        
        Theme.setScene(scene);
        Theme.apply("light");
        
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (connection != null) {
            connection.closeConnection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}