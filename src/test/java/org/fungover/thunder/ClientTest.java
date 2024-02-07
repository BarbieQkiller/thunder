package org.fungover.thunder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private Client client;
    private TopicHandler topicManager;

    @BeforeEach
    public void setUp() {
        topicManager = Mockito.mock(TopicHandler.class);
        client = new Client("TestClient", topicManager);
    }

    @Test
    void givenClientIdIsSetToTestClientThenGetClientIdShouldReturnTestClient() {
        assertEquals("TestClient", client.getClientId());
    }

    @Test
    void givenClientHasYetNotBeenConnectedThenIsConnectedShouldReturnFalse() {
        assertFalse(client.isConnected());
    }

    @Test
    void givenClientIsConnectedThenIsConnectedShouldReturnTrue() {
        client.connect();
        assertTrue(client.isConnected());
    }

    @Test
    void givenClientIsDisconnectedThenIsConnectedShouldReturnFalse() {
        client.disconnect();
        assertFalse(client.isConnected());
    }


}
