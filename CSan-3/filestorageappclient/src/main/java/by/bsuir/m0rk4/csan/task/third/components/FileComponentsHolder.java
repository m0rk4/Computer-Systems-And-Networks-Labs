package by.bsuir.m0rk4.csan.task.third.components;

import by.bsuir.m0rk4.csan.task.third.dto.FileDto;
import by.bsuir.m0rk4.csan.task.third.http.HttpClient;
import by.bsuir.m0rk4.csan.task.third.http.HttpException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileComponentsHolder extends VBox {

    private String currentPath = "/";
    private final Button backButton;

    public FileComponentsHolder(Button backButton) {
        super(5);
        this.backButton = backButton;
        backButton.setOnAction(e -> moveBack());
        backButton.setDisable(true);

        setOnDragOver(this::handleDragFromOutside);
        setOnDragDropped(this::handleDragDroppedFromOutside);
        
    }

    private void handleDragDroppedFromOutside(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            List<File> files = dragEvent.getDragboard().getFiles();
            for (File file : files) {
                try {
                    FileDto fileDto = HttpClient.getInstance().uploadFile(file, currentPath);
                    FileComponent fileComponent = new FileComponent(fileDto);
                    addFileComponent(fileComponent);
                } catch (HttpException e) {
                    showAlert("Internal Error: " + file.getName(), e.getMessage());
                }
            }
        }
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleDragFromOutside(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
        }
    }

    public void moveByDir(String dirName) {
        currentPath += dirName;
        if (currentPath.charAt(currentPath.length() - 1) != '/') {
            currentPath += '/';
        }
        backButton.setDisable(false);
    }

    public void moveBack() {
        String removedLast = currentPath.substring(0, currentPath.length() - 1);
        String futureDir = currentPath.substring(0, removedLast.lastIndexOf('/') + 1);
        try {
            List<FileDto> dirContents = HttpClient.getInstance().getDirContents(futureDir);
            List<FileComponent> collect = dirContents.stream()
                    .map(FileComponent::new)
                    .collect(Collectors.toList());
            setAll(collect);
            currentPath = futureDir;
            if (currentPath.equals("/")) {
                backButton.setDisable(true);
            }
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }

    public void addFileComponent(FileComponent fileComponent) {
        getChildren().add(fileComponent);
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setFileComponent(FileComponent fileComponent) {
        Optional<FileComponent> first = getChildren().stream()
                .map(node -> (FileComponent) node)
                .filter(fC -> fC.equals(fileComponent))
                .findFirst();
        if (first.isPresent()) {
            getChildren().remove(first.get());
            getChildren().add(fileComponent);
        }
    }

    public void deleteFileComponent(FileComponent fileComponent) {
        getChildren().remove(fileComponent);
    }

    public void setAll(List<FileComponent> fileComponents) {
        getChildren().setAll(fileComponents);
    }

    public void deleteFileComponentByName(String filename) {
        String substring = filename.substring(filename.lastIndexOf('/') + 1);
        Optional<FileComponent> first = getChildren().stream()
                .map(node -> (FileComponent) node)
                .filter(fileComponent -> fileComponent.getFilename().equals(substring))
                .findFirst();
        first.ifPresent(fileComponent -> getChildren().remove(fileComponent));
    }
}
