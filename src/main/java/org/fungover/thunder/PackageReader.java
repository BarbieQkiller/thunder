package org.fungover.thunder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PackageReader {
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final Logger logger = Logger.getLogger(PackageReader.class.getName());
    private final Map<InetAddress, Boolean> connectPackageSent = new HashMap<>();
    private final Subscription subscription = new Subscription();

    public boolean isValidConnection(Socket socket) throws IOException {
        InetAddress client = socket.getInetAddress();
        byte[] buffer = new byte[1024];
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        socket.setSoTimeout(CONNECTION_TIMEOUT);
        int bytesRead = inputStream.read(buffer);

        if (isDoubleConnectMessage(client, buffer)) {
            return false;
        }

        if (isConnectPackage(bytesRead, buffer)) {
            socket.setSoTimeout(0);
            logger.info("Received MQTT CONNECT message from client");
            connectPackageSent.put(socket.getInetAddress(), true);
            sendConnackToClient(outputStream);
            logger.info("Sent MQTT CONNACK message to client");
            return true;
        }

        logger.info("Received no MQTT CONNECT message. Disconnecting client " + client);
        return false;
    }

    private boolean isDoubleConnectMessage(InetAddress client, byte[] buffer) {
        return connectPackageSent.containsKey(client) && Boolean.TRUE.equals(connectPackageSent.get(client)) && buffer[0] == 0x10;
    }

    private static boolean isConnectPackage(int bytesRead, byte[] buffer) {
        return bytesRead > 0 && buffer[0] == (byte) 0x10;
    }

    public boolean readFromClient(Socket clientSocket) throws IOException {

        byte[] buffer = new byte[1024];
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
        int bytesRead = inputStream.read(buffer);

        if (buffer[0] == 0x10) {
            System.out.println("Sending connection package after connection is established is forbidden.");
            return false;
        }

        if (buffer[0] == (byte) 0x82) {
            subscription.read(bytesRead, buffer, clientSocket);
            sendSubackToClient(outputStream, buffer, bytesRead);
            return true;
        }

        if (isClientConnected(clientSocket.getInetAddress())) {
            return true;
        }

        return !isCleanDisconnect(clientSocket, buffer, bytesRead);
    }

    private static void sendMessageToClient(OutputStream outputStream, byte[] message) throws IOException {
        outputStream.write(message);
        outputStream.flush();
    }

    private static void sendConnackToClient(OutputStream outputStream) throws IOException {
        byte[] connackMessage = new byte[]{(byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        sendMessageToClient(outputStream, connackMessage);
    }

    private static void sendSubackToClient(OutputStream outputStream, byte[] packet, int bytesRead) throws IOException {
        byte[] subackMessage = new byte[]{
            (byte) 0x90,  // Fixed Header for SUBACK message
            (byte) 0x03,  // Remaining Length ,3 bytes for Packet Identifier and QoS level
            packet[2],  // Packet Identifier MSB
            packet[3],  // Packet Identifier LSB
            packet[bytesRead - 1]   // Index of the QoS level of the subscription
        };

        sendMessageToClient(outputStream, subackMessage);
    }

    private static boolean isDisconnectPackage(int bytesRead, byte[] buffer) {
        return bytesRead > 0 && buffer[0] == (byte) 0xE0;
    }


    public boolean isCleanDisconnect(Socket socket, byte[] buffer, int bytesRead) {
        InetAddress client = socket.getInetAddress();

        if (isClientConnected(client)) {
            return false;
        }

        if (isDisconnectPackage(bytesRead, buffer)) {
            logger.info("Received MQTT DISCONNECT message from client");
            connectPackageSent.remove(client);
            return true;
        }

        return false;
    }

    private boolean isClientConnected(InetAddress client) {
        return !connectPackageSent.containsKey(client);
    }
}
