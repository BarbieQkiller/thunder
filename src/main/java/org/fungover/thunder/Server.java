package org.fungover.thunder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocket serverSocket;
    private final ClientHandler clientHandler;

    public Server() throws IOException {
        this.clientHandler = new ClientHandler();
        this.serverSocket = new ServerSocket(1883);
    }

    public void start() throws IOException {
        logger.info("Server started on port 1883");
        try {
            while (!serverSocket.isClosed()) {
                Socket connection = serverSocket.accept();
                if (connection == null)
                    throw new NullPointerException("Connection is null");
                Thread.ofVirtual().start(() -> clientHandler.handleConnection(connection));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
