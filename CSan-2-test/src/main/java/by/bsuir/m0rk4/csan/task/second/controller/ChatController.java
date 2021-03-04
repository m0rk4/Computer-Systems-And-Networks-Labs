package by.bsuir.m0rk4.csan.task.second.controller;

import by.bsuir.m0rk4.csan.task.second.App;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ChatController {

    private volatile boolean running = true;

    public Button disconnectButton;
    private final Socket socket;
    private final String name;
    private final BufferedOutputStream outputStream;
    private final BufferedInputStream inputStream;

    public TextField messageTField;
    public Button sendMessageButton;
    public TextArea chatTArea;

    public Button sendFileButton;
    public VBox fileButtonsContainer;

    public ChatController(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
    }

    @FXML
    private void initialize() {
        disconnectButton.setOnAction(this::disconnect);
        sendMessageButton.setOnAction(this::sendMessage);
        sendFileButton.setOnAction(this::sendFile);

        startReadMessageThread();
    }

    private void startReadMessageThread() {
        new Thread(() -> {
            int len;
            byte[] buffer = new byte[4096 * 20];
            try {
                while (running) {
                    inputStream.read(buffer, 0, 18);
                    String contentType = new String(buffer, 0, 18);

                    inputStream.read(buffer, 0, 40);
                    String authorKeyValue = new String(buffer, 0, 40);
                    String author = authorKeyValue.split("\\s+")[1];

                    if ("Content-Type: Text".equals(contentType)) {

                        inputStream.read(buffer, 0, 24);
                        String lenKeyValue = new String(buffer, 0, 24);
                        String messageLenStr = lenKeyValue.split("\\s+")[1];
                        int messageLen = Integer.parseInt(messageLenStr);

                        int total = 0;
                        StringBuilder stringBuilder = new StringBuilder();
                        while (total != messageLen) {
                            len = inputStream.read(buffer);
                            total += len;
                            String str = new String(buffer, 0, len);
                            stringBuilder.append(str);
                        }
                        String text = stringBuilder.toString();
                        chatTArea.appendText(author + ": "  + text + "\n");
                    } else {
                        inputStream.read(buffer, 0, 138);
                        String filenameKeyValue = new String(buffer, 0, 138);
                        String filename = filenameKeyValue.substring(filenameKeyValue.indexOf(' ')).trim();

                        inputStream.read(buffer, 0, 24);
                        String lenKeyValue = new String(buffer, 0, 24);
                        String messageLenStr = lenKeyValue.split("\\s+")[1];
                        int fileLen = Integer.parseInt(messageLenStr);

                        byte[] fileContent = new byte[fileLen];
                        int total = 0;
                        while (total != fileLen) {
                            len = inputStream.read(fileContent, total, fileLen - total);
                            total += len;
                        }

                        CustomButtonFileDownloader customButtonFileDownloader =
                                new CustomButtonFileDownloader(filename, fileContent);

                        ObservableList<Node> children = fileButtonsContainer.getChildren();
                        Platform.runLater(()->{
                            children.add(new Label("Sender: " + author));
                            children.add(customButtonFileDownloader);
                            customButtonFileDownloader.setOnAction(this::downloadFile);
                        });

                    }
                }
            } catch (IOException e) {
                System.out.println("Socket: " + socket + " is closed. Bye " + name  + "!");
            }
        }).start();
    }

    private void downloadFile(ActionEvent actionEvent) {
        CustomButtonFileDownloader customButtonFileDownloader = (CustomButtonFileDownloader) actionEvent.getSource();
        String filename = customButtonFileDownloader.getFilename();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName(filename);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] contents = customButtonFileDownloader.getContents();
                bufferedOutputStream.write(contents,0 , contents.length);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Saving", "Can't save file.");
            }
        }
    }

    private void sendFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setTitle("Choose File");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                int read;
                byte[] buffer = new byte[4096 * 20];
                long fileLen = file.length();

                String contentType = "Content-Type: File";
                String author = "Author: " + String.format("%32s", name);
                String filename = "Filename: " + String.format("%128s", file.getName());
                String length = "Length: " + String.format("%16d", fileLen);

                outputStream.write(contentType.getBytes());
                outputStream.write(author.getBytes());
                outputStream.write(filename.getBytes());
                outputStream.write(length.getBytes());

                while ((read = bufferedInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File", "Can't load file.");
            }
        }
    }

    private void sendMessage(ActionEvent actionEvent) {
        try {

            String message = messageTField.getText();
            int messageLen = message.length();

            String contentType = "Content-Type: Text";
            String author = "Author: " + String.format("%32s", name);
            String length = "Length: " + String.format("%16d", messageLen);

            outputStream.write(contentType.getBytes());
            outputStream.write(author.getBytes());
            outputStream.write(length.getBytes());
            outputStream.write(message.getBytes());

            outputStream.flush();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Message", "Can't send message, I/O exception occurred.");
        }
    }

    private void disconnect(ActionEvent actionEvent) {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        moveToLogin();
    }

    private void moveToLogin() {
        App.switchToLogin();
    }

    private void showAlert(Alert.AlertType type, String title, String info) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(info);
        alert.show();
    }

}
