package org.fungover.thunder;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final List<Socket> clients;
    private final PackageReader packageReader;

    public ClientHandler() {
        clients = Collections.synchronizedList(new ArrayList<>());
        packageReader = new PackageReader();
    }

    public void handleConnection(Socket clientSocket) {
        try {
            if (packageReader.isValidConnection(clientSocket)) {
                logger.info("New client: " + clientSocket.getInetAddress().getHostName());
                clients.add(clientSocket);
            } else {
                clientSocket.close();
            }
            while (clients.contains(clientSocket)) {
                if (!packageReader.readFromClient(clientSocket)) {
                    clientSocket.close();
                    clients.remove(clientSocket);
                    break;
                }
            }
            removeDisconnectedClients();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void removeDisconnectedClients() {
        synchronized (clients) {
            Iterator<Socket> iterator = clients.iterator();
            while (iterator.hasNext()) {
                Socket client = iterator.next();
                if (client.isClosed()) {
                    logger.info("Client disconnected: " + client.getInetAddress().getHostName());
                    iterator.remove();
                }
            }
        }
    }

    public List<Socket> getClients() {
        return clients;
    }
}
