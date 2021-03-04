package by.bsuir.m0rk4.csan.task.second.controller;

import by.bsuir.m0rk4.csan.task.second.App;
import by.bsuir.m0rk4.csan.task.second.serverside.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.*;

public class LoginController {

    public TextField usernameTField;
    public TextField ipTField;
    public TextField portTField;

    public Button connectButton;

    @FXML
    private void initialize() {
        connectButton.setOnAction(this::connectEvent);
    }

    private void connectEvent(ActionEvent actionEvent) {
        String name = usernameTField.getText();
        if (name.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Name", "Enter correct name.");
            return;
        }

        String portString = portTField.getText();
        String ipString = ipTField.getText();

        Integer port = null;
        try {
            port = Integer.parseInt(portString);
            ServerSocket serverSocket = new ServerSocket(port);

            Server server = new Server(serverSocket);

            Socket socket = createClientSocket(ipString, port);
            server.start();
            moveToChat(socket, name);

        } catch (IllegalArgumentException e) {
            if (port == null) {
                showAlert(Alert.AlertType.ERROR, "Port", "Check port validity.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Port", "Port must be between 0 and 65535, inclusive.");
            }
        } catch (BindException e) {
            try {
                Socket socket = createClientSocket(ipString, port);
                moveToChat(socket, name);
            } catch (IOException ioException) {
                showAlert(Alert.AlertType.ERROR, "Connection", "Error occurred during the connection.");
            }
        } catch (SocketTimeoutException e) {
            showAlert(Alert.AlertType.ERROR, "Socket", "Can't create socket. Check IP & port.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Connection", "Error occurred during the connection.");
        }
    }

    private Socket createClientSocket(String ip, int port) throws IOException {
        Socket socket = new Socket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
        socket.connect(inetSocketAddress, 2000);
        return socket;
    }

    private void moveToChat(Socket socket, String name) {
        App.switchToChat(socket, name);
    }

    private void showAlert(Alert.AlertType type, String title, String info) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(info);
        alert.show();
    }
}
