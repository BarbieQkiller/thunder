package org.fungover.thunder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientHandlerTest {
    ClientHandler clientHandler;
    PackageReader packageReader;

    @BeforeEach
    void setUp() {
        clientHandler = new ClientHandler();
        packageReader = mock(PackageReader.class);
    }

    @Test
    @DisplayName("Return 0 if valid client is added and then disconnected")
    void return0IfValidClientIsAddedAndThenDisconnected() throws IOException {
        Socket socketMock = mock(Socket.class);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 1883);
        when(socketMock.getInetAddress()).thenReturn(inetSocketAddress.getAddress());
        InputStream connectPacket = new ByteArrayInputStream(new byte[]{0x10});
        InputStream disconnectPacket = new ByteArrayInputStream(new byte[]{(byte) 0xE0});
        OutputStream out = new ByteArrayOutputStream();
        when(socketMock.getOutputStream()).thenReturn(out);
        when(socketMock.getInputStream()).thenReturn(connectPacket, disconnectPacket);

        clientHandler.handleConnection(socketMock);

        assertEquals(0, clientHandler.getClients().size());
    }

    @Test
    @DisplayName("Return 0 if not valid client connects")
    void return0IfNotValidClientConnects() throws IOException {
        Socket socketMock = mock(Socket.class);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 1883);
        when(socketMock.getInetAddress()).thenReturn(inetSocketAddress.getAddress());
        InputStream in = new ByteArrayInputStream(new byte[]{0x20});
        when(socketMock.getInputStream()).thenReturn(in);

        clientHandler.handleConnection(socketMock);

        assertEquals(0, clientHandler.getClients().size());
    }

    @Test
    @DisplayName("Should remove disconnected clients")
    void shouldRemoveDisconnectedClients() throws IOException {
        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        when(socket1.isClosed()).thenReturn(true);
        when(socket2.isClosed()).thenReturn(false);
        when(socket1.getInetAddress()).thenReturn(InetAddress.getByName("localhost"));
        when(socket2.getInetAddress()).thenReturn(InetAddress.getByName("localhost"));

        clientHandler.getClients().addAll(List.of(socket1, socket2));
        clientHandler.removeDisconnectedClients();

        assertEquals(Collections.singletonList(socket2), clientHandler.getClients());
    }
    @Test
    @DisplayName("Should log using java util logging")
    void shouldLogUsingJavaUtilLogging() throws IOException{
        Logger logger = Logger.getLogger("");
        SpyLogHandler spyLogHandler = new SpyLogHandler();
        logger.addHandler(spyLogHandler);

        clientHandler.handleConnection(null);

        assertNotNull(spyLogHandler.getLastRecord());
        assertEquals(Level.WARNING, spyLogHandler.getLastRecord().getLevel());
        assertEquals("Cannot invoke \"java.net.Socket.getInetAddress()\" because \"socket\" is null", spyLogHandler.getLastRecord().getMessage());

        logger.removeHandler(spyLogHandler);
    }

}
