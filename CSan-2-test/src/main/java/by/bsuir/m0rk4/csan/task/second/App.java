package by.bsuir.m0rk4.csan.task.second;

import by.bsuir.m0rk4.csan.task.second.controller.ChatController;
import by.bsuir.m0rk4.csan.task.second.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final String LOGIN_FXML_NAME = "login.fxml";
    private static final String CHAT_FXML_NAME = "chat.fxml";
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        URL resource = App.class.getResource(LOGIN_FXML_NAME);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);

        Parent root = fxmlLoader.load();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    public static void switchToChat(Socket socket, String name) {
        try {
            URL resource = App.class.getResource(CHAT_FXML_NAME);
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            fxmlLoader.setControllerFactory(c -> {
                try {
                    return new ChatController(socket, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
            Parent root = fxmlLoader.load();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("ERROR: IO error occurred.");
        }
    }

    public static void switchToLogin() {
        try {
            URL resource = App.class.getResource(LOGIN_FXML_NAME);
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Parent root = fxmlLoader.load();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("ERROR: IO error occurred.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}