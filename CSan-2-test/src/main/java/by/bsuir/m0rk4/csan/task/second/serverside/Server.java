package by.bsuir.m0rk4.csan.task.second.serverside;

import by.bsuir.m0rk4.csan.task.second.clientside.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {

    private final ServerSocket serverSocket;
    private final List<ClientHandler> handlers;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        handlers = new Vector<>();
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket createdSocket = serverSocket.accept();
                    System.out.println("Socket connected: " + createdSocket);
                    ClientHandler clientHandler = new ClientHandler(createdSocket, this);
                    handlers.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public List<ClientHandler> getHandlers() {
        return handlers;
    }
}


