import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.ChatView;

public class ChatClientFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Solution 1 : ChatView contient tout
        ChatView chatView = new ChatView();
        
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(new Scene(chatView, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}