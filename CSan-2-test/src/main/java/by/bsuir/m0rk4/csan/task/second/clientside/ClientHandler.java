package by.bsuir.m0rk4.csan.task.second.clientside;

import by.bsuir.m0rk4.csan.task.second.serverside.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Server ownerServer;
    private final BufferedOutputStream outputStream;
    private final BufferedInputStream inputStream;
    private final Socket clientRemoteSocket;

    public ClientHandler(Socket createdSocket, Server server) throws IOException {
        clientRemoteSocket = createdSocket;
        ownerServer = server;
        outputStream = new BufferedOutputStream(createdSocket.getOutputStream());
        inputStream = new BufferedInputStream(createdSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            int len;
            byte[] buffer = new byte[4096 * 20];
            while (true) {
                List<ClientHandler> handlers = ownerServer.getHandlers();

                if (inputStream.read(buffer, 0, 18) == -1) {
                    break;
                }

                System.out.println("\n------------------------------------------------------------");
                System.out.println("Info received: ");
                System.out.println("------------------------------------------------------------");

                String contentType = new String(buffer, 0, 18);
                System.out.println(contentType);

                for (ClientHandler handler : handlers) {
                    handler.outputStream.write(buffer, 0, 18);
                }

                inputStream.read(buffer, 0, 40);
                String authorKeyValue = new String(buffer, 0, 40);
                System.out.println(authorKeyValue);

                for (ClientHandler handler : handlers) {
                    handler.outputStream.write(buffer, 0, 40);
                }

                if ("Content-Type: File".equals(contentType)) {
                    inputStream.read(buffer, 0, 138);
                    String filenameKeyValue = new String(buffer, 0, 138);
                    System.out.println(filenameKeyValue);

                    for (ClientHandler handler : handlers) {
                        handler.outputStream.write(buffer, 0, 138);
                    }
                }

                inputStream.read(buffer, 0, 24);
                String lenKeyValue = new String(buffer, 0, 24);
                System.out.println(lenKeyValue);

                String messageLenStr = lenKeyValue.substring(lenKeyValue.indexOf(' ')).trim();
                int messageLen = Integer.parseInt(messageLenStr);

                for (ClientHandler handler : handlers) {
                    handler.outputStream.write(buffer, 0, 24);
                }

                System.out.println("***CONTENT***");
                int total = 0;
                while (total != messageLen) {
                    len = inputStream.read(buffer);
                    total += len;
                    for (ClientHandler handler : handlers) {
                        handler.outputStream.write(buffer, 0, len);
                    }
                }

                for (ClientHandler handler : handlers) {
                    handler.outputStream.flush();
                }
                System.out.println("------------------------------------------------------------");
            }
        } catch (IOException e) {
            System.err.println("Something bad happened during data transmission.");
        }

        ownerServer.removeHandler(this);

        try {
            clientRemoteSocket.close();
            System.out.println("Remote client socket: " + clientRemoteSocket + " is closed.");
        } catch (IOException e) {
            System.err.println("Failed to close remote client socket.");
        }

        try {
            List<ClientHandler> handlers = ownerServer.getHandlers();
            if (handlers.isEmpty()) {
                ServerSocket serverSocket = ownerServer.getServerSocket();
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close server socket.");
        }

    }
}
