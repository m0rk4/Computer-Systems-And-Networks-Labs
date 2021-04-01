package by.bsuir.m0rk4.csan.task.third;

import by.bsuir.m0rk4.csan.task.third.components.FileComponent;
import by.bsuir.m0rk4.csan.task.third.components.FileComponentsHolder;
import by.bsuir.m0rk4.csan.task.third.dto.FileDto;
import by.bsuir.m0rk4.csan.task.third.http.HttpClient;
import by.bsuir.m0rk4.csan.task.third.http.HttpException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final String APP_TITLE = "Simple File Storage App";
    private static final String MAIN_VIEW = "primary.fxml";

    @Override
    public void init() throws FileNotFoundException {

    }

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = App.class.getResource(MAIN_VIEW);
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        SplitPane splitPane = fxmlLoader.load();

        splitPane.setStyle("-fx-background-color: white");
        splitPane.getItems().stream()
                .map(node -> (BorderPane) node)
                .forEach(this::createContent);

        Scene scene = new Scene(splitPane);
        stage.setScene(scene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    private void createContent(BorderPane borderPane)  {
        Button backButton = new Button("Go back");
        borderPane.setTop(backButton);
        FileComponentsHolder fileComponentsHolder = new FileComponentsHolder(backButton);
        List<FileDto> dirContents = null;
        try {
            dirContents = HttpClient.getInstance().getDirContents("/");
            List<FileComponent> fileComponents = dirContents.stream()
                    .map(FileComponent::new)
                    .collect(Collectors.toList());
            fileComponentsHolder.setAll(fileComponents);
        } catch (HttpException e) {
            showAlert("Internal Error", e.getMessage());
        }
        borderPane.setCenter(fileComponentsHolder);
    }

    public static void main(String[] args) {
        launch();
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}