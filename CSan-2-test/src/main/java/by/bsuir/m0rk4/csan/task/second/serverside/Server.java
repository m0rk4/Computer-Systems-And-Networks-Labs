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

    public void listen() {
        new Thread(() -> {
            System.out.println("Server socket: " + serverSocket + " STARTED LISTENING.");
            try {
                while (true) {
                    Socket createdSocket = serverSocket.accept();
                    System.out.println("Remote client socket connected: " + createdSocket);
                    ClientHandler clientHandler = new ClientHandler(createdSocket, this);
                    handlers.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                System.out.println("Server socket: " + serverSocket + " is closed.");
            }
        }).start();
    }

    public List<ClientHandler> getHandlers() {
        return handlers;
    }

    public void removeHandler(ClientHandler clientHandler) {
        handlers.remove(clientHandler);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}


