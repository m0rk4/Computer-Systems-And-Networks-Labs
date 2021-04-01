package by.bsuir.m0rk4.csan.task.third.components;

import by.bsuir.m0rk4.csan.task.third.App;
import by.bsuir.m0rk4.csan.task.third.dto.FileDto;
import by.bsuir.m0rk4.csan.task.third.http.HttpClient;
import by.bsuir.m0rk4.csan.task.third.http.HttpException;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileComponent extends HBox {

    private ImageView icon;
    private String filename;
    private long size;


    public FileComponent(FileDto fileDto) {

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isPrimaryButtonDown() && fileDto.isDirectory()) {
                openDir(null);
                event.consume();
            }
        });

        if (fileDto.isDirectory()) {
            setCursor(Cursor.HAND);
        }

        if (fileDto.isDirectory()) {
            setOnDragOver(this::handleDragFromOutside);
            setOnDragDropped(this::handleDragDroppedFromOutside);
        }
        if (!fileDto.isDirectory()) {
            setOnDragDetected(this::handleDragDetectedFromInside);
        }

        setSpacing(20);
        setPadding(new Insets(5));
        setStyle("-fx-border-color: black");

        try {
            if (fileDto.isDirectory()) {
                icon = new ImageView(new Image(new FileInputStream(
                        App.class.getResource("folderlogo.png").getPath()),
                        20.0, 20.0, false, false)
                );
            } else {
                icon = new ImageView(new Image(new FileInputStream(
                        App.class.getResource("filelogo.png").getPath()),
                        22.0, 20.0, false, false)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(icon);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem item3 = new MenuItem("Append from clipboard");
        MenuItem pasteItem = new MenuItem("Paste file");
        if (fileDto.isDirectory()) {
            MenuItem item = new MenuItem("Open");
            MenuItem item2 = new MenuItem("Delete");
            item.setOnAction(this::openDir);
            item2.setOnAction(this::deleteDir);
            pasteItem.setDisable(true);
            pasteItem.setOnAction(this::pasteFile);
            contextMenu.getItems().addAll(item, item2, pasteItem);
        } else {
            MenuItem item = new MenuItem("Download");
            MenuItem item2 = new MenuItem("Delete");
            MenuItem item4 = new MenuItem("Copy");
            item3.setDisable(true);
            item.setOnAction(this::loadFile);
            item2.setOnAction(this::deleteFile);
            item3.setOnAction(this::appendContent);
            item4.setOnAction(e -> {
                FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
                String currentPath = fileComponentsHolder.getCurrentPath();
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putUrl(currentPath + filename);
                Clipboard.getSystemClipboard().setContent(clipboardContent);
            });
            contextMenu.getItems().addAll(item, item2, item3, item4);
        }

        setOnContextMenuRequested(e -> {
            if (Clipboard.getSystemClipboard().hasString()) {
                Optional<MenuItem> first = contextMenu.getItems().stream().filter(i -> i.equals(item3)).findFirst();
                first.ifPresent(menuItem -> menuItem.setDisable(false));
            } else if (Clipboard.getSystemClipboard().hasUrl()) {
                Optional<MenuItem> first = contextMenu.getItems().stream().filter(i -> i.equals(pasteItem)).findFirst();
                first.ifPresent(menuItem -> menuItem.setDisable(false));
            }
            contextMenu.show(this, e.getScreenX(), e.getScreenY());
        });

        filename = fileDto.getFilename();
        Label e = new Label(filename);
        e.setFont(new Font("Times New Roman", 18));
        getChildren().add(e);

        size = fileDto.getSize();
        Label e1 = new Label(Long.toString(size));
        e1.setFont(new Font("Times New Roman", 18));
        getChildren().add(e1);
    }

    private void pasteFile(ActionEvent actionEvent) {
        if (Clipboard.getSystemClipboard().hasUrl()) {
            FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
            String currentPath = fileComponentsHolder.getCurrentPath();
            String filename = Clipboard.getSystemClipboard().getUrl();
            try {
                HttpClient.getInstance().copyFile(filename,
                        currentPath +
                                this.filename +
                                "/" +
                                filename.substring(filename.lastIndexOf('/') + 1));
            } catch (HttpException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendContent(ActionEvent actionEvent) {
        if (Clipboard.getSystemClipboard().hasString()) {
            String content = Clipboard.getSystemClipboard().getString();
            FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
            String currentPath = fileComponentsHolder.getCurrentPath();
            try {
                FileDto fileDto = HttpClient.getInstance().appendContentToFile(currentPath + filename, content);
                FileComponent fileComponent = new FileComponent(fileDto);
                fileComponentsHolder.addFileComponent(fileComponent);
                fileComponentsHolder.deleteFileComponent(this);
            } catch (HttpException e) {
                showAlert("Internal Error", e.getMessage());
            }
        }
    }


    private void deleteFile(ActionEvent actionEvent) {
        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();
        try {
            HttpClient.getInstance().deleteFile(currentPath + filename);
            fileComponentsHolder.deleteFileComponent(this);
        } catch (HttpException e) {
            showAlert("Internal Error", e.getMessage());
        }
    }

    private void deleteDir(ActionEvent actionEvent) {
        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();
        try {
            HttpClient.getInstance().deleteDirectory(currentPath + filename);
            fileComponentsHolder.deleteFileComponent(this);
        } catch (HttpException e) {
            showAlert("Internal Error", e.getMessage());
        }
    }

    private void loadFile(ActionEvent actionEvent) {
        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setTitle("Select file");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                InputStream inputStream = HttpClient.getInstance().downloadFile(currentPath + filename);
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (HttpException e) {
                showAlert("Internal Error", e.getMessage());
            } catch (IOException e) {
                showAlert("Copy Error", e.getMessage());
            }
        }
    }


    private void openDir(ActionEvent actionEvent) {
        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();
        try {
            List<FileDto> dirContents = HttpClient.getInstance().getDirContents(currentPath + filename);
            List<FileComponent> components = dirContents.stream()
                    .map(FileComponent::new)
                    .collect(Collectors.toList());
            fileComponentsHolder.moveByDir(filename);
            fileComponentsHolder.setAll(components);
        } catch (HttpException e) {
            showAlert("Internal Error", e.getMessage());
        }
    }

    private void handleDragDetectedFromInside(MouseEvent event) {
        Dragboard dragboard = startDragAndDrop(TransferMode.ANY);

        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(currentPath + filename);

        dragboard.setContent(clipboardContent);

        event.consume();
    }

    private void handleDragDroppedFromOutside(DragEvent dragEvent) {
        FileComponentsHolder fileComponentsHolder = (FileComponentsHolder) getParent();
        String currentPath = fileComponentsHolder.getCurrentPath();
        HttpClient httpClient = HttpClient.getInstance();
        if (dragEvent.getDragboard().hasFiles()) {
            List<File> files = dragEvent.getDragboard().getFiles();
            for (File file : files) {
                try {
                    httpClient.uploadFile(file, currentPath + filename);
                } catch (HttpException e) {
                    showAlert("Internal Error", e.getMessage());
                }
            }
        } else if (dragEvent.getDragboard().hasString()) {
            String filename = dragEvent.getDragboard().getString();
            try {
                httpClient.moveFile(filename,
                        currentPath +
                                this.filename +
                                "/" +
                                filename.substring(filename.lastIndexOf('/') + 1));
                fileComponentsHolder.deleteFileComponentByName(filename);
            } catch (HttpException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleDragFromOutside(DragEvent e) {
        if (e.getDragboard().hasFiles() || e.getDragboard().hasString()) {
            e.acceptTransferModes(TransferMode.ANY);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileComponent that = (FileComponent) o;
        return size == that.size &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, filename, size);
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public long getSize() {
        return size;
    }

    public String getFilename() {
        return filename;
    }
}
