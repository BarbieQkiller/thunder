package org.fungover.thunder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Server server;
        try {
            server = new Server();
            server.start();
        } catch (IOException | NullPointerException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
